package songbox.house.service.search.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.domain.SearchStatus;
import songbox.house.domain.dto.request.SearchRequestDto;
import songbox.house.domain.dto.request.TrackListParsingResultDto;
import songbox.house.domain.dto.request.TrackListSearchRequestDto;
import songbox.house.domain.dto.response.SearchAndDownloadResponseDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.entity.SearchHistory;
import songbox.house.domain.entity.TrackListPattern;
import songbox.house.exception.ParsingException;
import songbox.house.repository.TrackListPatternRepository;
import songbox.house.service.search.SearchHistoryService;
import songbox.house.service.search.TrackDownloadService;
import songbox.house.service.search.TrackListSearchService;
import songbox.house.util.ArtistsTitle;
import songbox.house.util.parser.TrackListParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Transactional
@Slf4j
public class TrackListSearchServiceImpl implements TrackListSearchService {

    TrackListPatternRepository patternRepository;
    TrackDownloadService trackDownloadService;
    SearchHistoryService searchHistoryService;

    @Override
    public SearchAndDownloadResponseDto searchAndDownloadTrackList(final TrackListSearchRequestDto requestDto) {
        final TrackListPattern pattern = getTrackListPattern(requestDto);

        final TrackListParsingResultDto parsingResult =
                TrackListParser.parseTrackList(requestDto.getTrackList(), pattern.getValue(), requestDto.getSeparator());

        final List<TrackDto> downloaded = new ArrayList<>();

        final List<String> notParsed = parsingResult.getNotParsed();
        notParsed.forEach(titleAuthor -> {
            final SearchHistory searchHistory = new SearchHistory();
            searchHistory.setSearchStatus(SearchStatus.FAIL);
            searchHistory.setTitle(titleAuthor);
            searchHistoryService.save(searchHistory);
        });

        parsingResult.getArtistTitles().forEach(artistTitleDto -> {
            final SearchRequestDto searchRequestDto = createSearchRequestDto(artistTitleDto, requestDto.getGenres(), requestDto.getCollectionId());

            trackDownloadService.searchAndDownload(searchRequestDto)
                    .map(downloaded::add)
                    .orElseGet(() -> notParsed.add(artistTitleDto.getArtists() + " - " + artistTitleDto.getTitle()));
        });

        return createResponseDto(notParsed, downloaded);
    }

    private TrackListPattern getTrackListPattern(final TrackListSearchRequestDto requestDto) {
        return patternRepository.findById(requestDto.getPatternId())
                .orElseThrow(() -> new ParsingException("Can't find pattern by id " + requestDto.getPatternId()));
    }

    @Override
    public void searchAndDownloadTrackListAsync(final TrackListSearchRequestDto requestDto) {
        final TrackListPattern pattern = getTrackListPattern(requestDto);

        final TrackListParsingResultDto parsingResult =
                TrackListParser.parseTrackList(requestDto.getTrackList(), pattern.getValue(), requestDto.getSeparator());

        parsingResult.getArtistTitles().forEach(artistTitleDto -> {
            final SearchRequestDto searchRequestDto = createSearchRequestDto(artistTitleDto, requestDto.getGenres(), requestDto.getCollectionId());
            trackDownloadService.searchAndDownloadAsync(searchRequestDto);
        });
    }

    private SearchRequestDto createSearchRequestDto(final ArtistsTitle artistTitle, final Set<String> genres,
            final Long collectionId) {
        final SearchRequestDto searchRequestDto = new SearchRequestDto();

        searchRequestDto.setTitle(artistTitle.getTitle());
        searchRequestDto.setArtists(artistTitle.getArtists());
        searchRequestDto.setGenres(genres);
        searchRequestDto.setCollectionId(collectionId);

        return searchRequestDto;
    }

    private SearchAndDownloadResponseDto createResponseDto(final List<String> notParsed,
            final List<TrackDto> downloaded) {
        final SearchAndDownloadResponseDto result = new SearchAndDownloadResponseDto();
        result.setFound(downloaded);
        result.setNotFound(notParsed);
        return result;
    }
}
