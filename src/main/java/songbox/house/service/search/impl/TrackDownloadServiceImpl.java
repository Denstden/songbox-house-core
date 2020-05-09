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
import songbox.house.util.ArtistsTitle;
import songbox.house.util.Measurable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.isNull;
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
    @Measurable
    public Optional<TrackDto> searchAndDownload(final SearchRequestDto searchRequest) {
        final ArtistsTitle artistsTitle = getArtistsTitle(searchRequest);
        final String authors = artistsTitle.getArtists();
        final String title = artistsTitle.getTitle();
        return searchAndDownload(new SearchQueryDto(authors + " - " + title), downloadServiceFacade::download,
                searchRequest.getCollectionId(), null, searchRequest.getGenres(), artistsTitle);
    }

    @Override
    public Optional<TrackDto> download(TrackMetadataDto trackMetadataDto, Long collectionId,
            Long ownerId, Set<String> genres) {

        final ArtistsTitle artistsTitle = trackMetadataDto.getArtistsTitle();
        return searchAndDownload(trackMetadataDto, downloadServiceFacade::download, collectionId, ownerId, genres,
                artistsTitle);
    }

    private <T> Optional<TrackDto> searchAndDownload(T metadata, Function<T, Optional<TrackDto>> downloadFunction,
            Long collectionId, Long ownerId, Set<String> genres, ArtistsTitle artistsTitle) {

        final String authors = artistsTitle.getArtists();
        final String title = artistsTitle.getTitle();

        final SearchHistory searchHistory = createSearchHistory(authors, title);

        return ofNullable(trackService.findByArtistAndTitle(authors, title))
                .map(fromDB -> {
                    log.debug("Found track in db, not perform searching.");
                    searchHistoryService.saveSuccess(searchHistory, fromDB, true);
                    return of(trackDtoConverter.toDto(fromDB));
                })
                .orElseGet(() -> downloadFunction.apply(metadata)
                        .map(track -> saveTrack(track, collectionId, ownerId, genres)));
    }

    @Override
    @Async
    public void searchAndDownloadAsync(final SearchRequestDto searchRequest) {
        final ArtistsTitle artistsTitle = getArtistsTitle(searchRequest);
        final String authors = artistsTitle.getArtists().trim();
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
    @Measurable
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
    @Measurable
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

    private TrackDto saveTrack(TrackDto trackDto, Long collectionId, @Nullable Long ownerId, Set<String> genres) {
        final Track track = trackDtoConverter.toEntity(trackDto);
        if (isNull(ownerId)) {
            trackService.save(track, genres, collectionId);
        } else {
            trackService.save(track, genres, collectionId, ownerId);
        }
        return trackDto;
    }

    //TODO refactor
    private Optional<TrackDto> downloadAndSaveTrack(final SearchRequestDto searchRequest, String authors,
            String title) {
        final Optional<TrackDto> trackDto = downloadServiceFacade.download(new SearchQueryDto(authors + " - " + title));
        trackDto.map(trackDtoConverter::toEntity)
                .ifPresent(track -> trackService.save(track, searchRequest.getGenres(), searchRequest.getCollectionId()));
        return trackDto;
    }

    private TrackMetadataDto fromSongDto(SongDto songDto) {
        TrackMetadataDto trackMetadataDto = new TrackMetadataDto();
        trackMetadataDto.setArtistsTitle(ArtistsTitle.of(songDto.getArtist(), songDto.getTitle()));
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

    private ArtistsTitle getArtistsTitle(final SearchRequestDto searchRequestDto) {
        if (isBlank(searchRequestDto.getArtists())) {
            return ArtistsTitle.parse(searchRequestDto.getTitle());
        } else if (isBlank(searchRequestDto.getTitle())) {
            return ArtistsTitle.parse(searchRequestDto.getArtists());
        } else {
            return ArtistsTitle.of(searchRequestDto.getArtists(), searchRequestDto.getTitle());
        }
    }
}
