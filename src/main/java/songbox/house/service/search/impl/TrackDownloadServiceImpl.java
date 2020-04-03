package songbox.house.service.search.impl;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import songbox.house.converter.TrackDtoConverter;
import songbox.house.domain.dto.request.ArtistTitleDto;
import songbox.house.domain.dto.request.SaveSongsDto;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.request.SearchRequestDto;
import songbox.house.domain.dto.response.SongDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.domain.entity.SearchHistory;
import songbox.house.domain.entity.Track;
import songbox.house.exception.NotExistsException;
import songbox.house.service.TrackService;
import songbox.house.service.search.DownloadServiceFacade;
import songbox.house.service.search.SearchHistoryService;
import songbox.house.service.search.TrackDownloadService;
import songbox.house.util.ArtistTitleUtil;
import songbox.house.util.Pair;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
@AllArgsConstructor
@Slf4j
public class TrackDownloadServiceImpl implements TrackDownloadService {

    SearchHistoryService searchHistoryService;
    DownloadServiceFacade downloadServiceFacade;
    TrackService trackService;
    TrackDtoConverter trackDtoConverter;

    @Override
    public Optional<TrackDto> searchAndDownload(final SearchRequestDto searchRequest) {
        final ArtistTitleDto artistsTitle = getArtistsTitle(searchRequest);
        final String authors = artistsTitle.getArtist().trim();
        final String title = artistsTitle.getTitle().trim();

        final SearchHistory searchHistory = createSearchHistory(authors, title);

        return ofNullable(trackService.findByArtistAndTitle(authors, title))
                .map(fromDB -> {
                    log.debug("Found track in db, not perform searching.");
                    searchHistoryService.saveSuccess(searchHistory, fromDB, true);
                    return of(trackDtoConverter.toDto(fromDB));
                })
                .orElseGet(() -> downloadAndSaveTrack(searchRequest, authors, title));
    }

    @Override
    @Async
    public void searchAndDownloadAsync(final SearchRequestDto searchRequest) {
        final ArtistTitleDto artistsTitle = getArtistsTitle(searchRequest);
        final String authors = artistsTitle.getArtist().trim();
        final String title = artistsTitle.getTitle().trim();

        final SearchHistory searchHistory = createSearchHistory(authors, title);

        final Track fromDb = trackService.findByArtistAndTitle(authors, title);
        if (fromDb != null) {
            log.debug("Found track in db, not perform searching.");
            searchHistoryService.saveSuccess(searchHistory, fromDb, true);
        } else {
            downloadAndSaveTrack(searchRequest, authors, title);
        }
    }


    @Override
    public Iterable<TrackDto> download(SaveSongsDto saveSongsDto) {
        final List<TrackDto> result = Lists.newArrayList();
        final Long collectionId = saveSongsDto.getCollectionId();

        final Set<SongDto> songs = saveSongsDto.getSongs();

        if (!isEmpty(songs)) {
            songs.forEach(songDto ->
                    downloadOne(collectionId, songDto)
                            .ifPresent(result::add)
            );
        }

        return result;
    }

    @Override
    public TrackDto download(SongDto songDto, Long collectionId) {
        return downloadOne(collectionId, songDto)
                .orElseThrow(() -> new NotExistsException("Exception during track downloading"));
    }

    private Optional<TrackDto> downloadOne(Long collectionId, SongDto songDto) {
        TrackMetadataDto trackMetadataDto = fromSongDto(songDto);
        final Optional<TrackDto> trackDto = downloadServiceFacade.download(trackMetadataDto);
        trackDto.map(trackDtoConverter::toEntity)
                .ifPresent(track -> trackService.save(track, songDto.getGenres(), collectionId));
        return trackDto;
    }

    private Optional<TrackDto> downloadAndSaveTrack(final SearchRequestDto searchRequest, String authors,
            String title) {
        final Optional<TrackDto> trackDto = downloadServiceFacade.download(new SearchQueryDto(authors + " - " + title));
        trackDto.map(trackDtoConverter::toEntity)
                .ifPresent(track -> trackService.save(track, searchRequest.getGenres(), searchRequest.getCollectionId()));
        return trackDto;
    }

    private TrackMetadataDto fromSongDto(SongDto songDto) {
        TrackMetadataDto trackMetadataDto = new TrackMetadataDto();
        trackMetadataDto.setArtists(songDto.getArtist());
        trackMetadataDto.setTitle(songDto.getTitle());
        trackMetadataDto.setDurationSec(songDto.getDuration());
        trackMetadataDto.setBitRate(songDto.getBitRate());
        trackMetadataDto.setThumbnail(songDto.getThumbnail());
        trackMetadataDto.setUri(songDto.getUri());
        trackMetadataDto.setResource(songDto.getResource());
        return trackMetadataDto;
    }

    private SearchHistory createSearchHistory(final String authors, final String title) {
        final SearchHistory searchHistory = new SearchHistory();
        searchHistory.setArtists(authors);
        searchHistory.setTitle(title);
        return searchHistory;
    }

    private ArtistTitleDto getArtistsTitle(final SearchRequestDto searchRequestDto) {
        if (isBlank(searchRequestDto.getArtists())) {
            final Pair<String, String> artistTitle = ArtistTitleUtil.extractArtistTitle(searchRequestDto.getTitle());
            return new ArtistTitleDto(artistTitle.getLeft(), artistTitle.getRight());
        } else if (isBlank(searchRequestDto.getTitle())) {
            final Pair<String, String> artistTitle = ArtistTitleUtil.extractArtistTitle(searchRequestDto.getArtists());
            return new ArtistTitleDto(artistTitle.getLeft(), artistTitle.getRight());
        } else {
            return new ArtistTitleDto(searchRequestDto.getArtists(), searchRequestDto.getTitle());
        }
    }
}
