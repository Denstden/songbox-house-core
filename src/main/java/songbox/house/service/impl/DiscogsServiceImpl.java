package songbox.house.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.SearchRequestDto;
import songbox.house.domain.dto.response.DiscogsTrackListResponseDto;
import songbox.house.domain.dto.response.SearchAndDownloadResponseDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.dto.response.discogs.DiscogsArtistDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseResponseDto;
import songbox.house.domain.dto.response.discogs.DiscogsTrackDto;
import songbox.house.domain.dto.response.discogs.DiscogsUserWantListDto;
import songbox.house.domain.dto.response.discogs.DiscogsUserWantListItemDto;
import songbox.house.domain.dto.response.discogs.ReleasePageable;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.exception.DiscogsException;
import songbox.house.service.DiscogsFacade;
import songbox.house.service.DiscogsService;
import songbox.house.service.MusicCollectionService;
import songbox.house.service.search.TrackDownloadService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.jsoup.helper.StringUtil.join;
import static org.springframework.util.CollectionUtils.isEmpty;
import static songbox.house.service.DiscogsFacade.DEFAULT_ARTISTS_DELIMITER;
import static songbox.house.util.StringUtils.removeEndingNumberInBrackets;


@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DiscogsServiceImpl implements DiscogsService {
    DiscogsFacade discogsFacade;

    TrackDownloadService searchService;
    MusicCollectionService collectionService;

    @Override
    public SearchAndDownloadResponseDto searchAndDownload(final String link, final Long collectionId) {
        final String releaseId = discogsFacade.getReleaseId(link);

        return discogsFacade.getReleaseDto(releaseId)
                .map(rel -> processDiscogsRelease(rel, collectionId))
                .orElseThrow(() -> new DiscogsException("Error getting release by link " + link));
    }

    @Override
    @Async
    public void searchAndDownloadAsync(String link, Long collectionId) {
        final String releaseId = discogsFacade.getReleaseId(link);

        processDiscogsReleaseIdAsync(releaseId, collectionId, false);
    }

    @Override
    @Async
    public void searchAndDownloadLinksAsync(String links, Long collectionId, String separator) {
        final List<String> releaseIds = getReleaseIds(links, separator);

        releaseIds.forEach(releaseId -> processDiscogsReleaseIdAsync(releaseId, collectionId, false));
    }

    private List<String> getReleaseIds(String links, String separator) {
        String[] splitted = links.split(separator);

        return stream(splitted)
                .map(discogsFacade::getReleaseId)
                .collect(toList());
    }

    @Override
    public List<DiscogsTrackListResponseDto> getTrackList(final String link) {
        final String releaseId = discogsFacade.getReleaseId(link);

        return discogsFacade.getReleaseDto(releaseId)
                .map(release -> toTrackListDto(release.getTracklist(), concatArtists(release.getArtists())))
                .orElseThrow(() -> new DiscogsException("Error getting track list by link " + link));
    }

    @Override
    @Async
    @SneakyThrows
    public void searchAndDownloadLabelTracks(final String labelLink, final Long collectionId, boolean only320) {
        final String labelId = discogsFacade.extractLabelId(labelLink);

        processDiscogsPageableRequest(collectionId, labelId, discogsFacade::getLabelReleases, false);

        log.info("Processing label releases finished.");
    }

    @Override
    @Async
    public void searchAndDownloadUserDiscogsCollection(final String userName, final String collectionName) {
        final MusicCollection collection = collectionService.getOrCreate(collectionName);

        processDiscogsPageableRequest(collection.getCollectionId(), userName, discogsFacade::getUserCollectionReleases, false);

        log.info("Processing user collection finished.");
    }

    @Override
    @Async
    public void searchAndDownloadArtistReleases(String link, Long collectionId, boolean only320) {
        final String userId = discogsFacade.extractArtistId(link).toString();

        processDiscogsPageableRequest(collectionId, userId, discogsFacade::getArtistReleases, only320);

        log.info("Processing artist releases finished.");
    }

    @Override
    @Async
    public void searchAndDownloadUserWantList(String userName, String collectionName) {
        final MusicCollection collection = collectionService.getOrCreate(collectionName);

        int pageNumber = 1;
        int countPages;
        int i = 1;

        do {
            DiscogsUserWantListDto userWantListReleaseIds = discogsFacade.getUserWantListReleaseIds(userName, pageNumber);
            countPages = userWantListReleaseIds.getPagination().getPages();

            userWantListReleaseIds.getWants().stream()
                    .map(DiscogsUserWantListItemDto::getId)
                    .forEach(id -> processDiscogsReleaseIdAsync(id.toString(), collection.getCollectionId(), false));
            log.info("Processed {} page of {}", pageNumber, countPages);

            pageNumber = countPages - i + 1;
        } while (i++ != countPages);

        log.info("Processing user want list finished.");
    }

    private <T extends ReleasePageable> void processDiscogsPageableRequest(long collectionId, String entityId,
            BiFunction<String, Integer, T> getDtoFunction, boolean only320) {

        int pageNumber = 1;
        int countPages;

        do {
            final T releasesPageable = getDtoFunction.apply(entityId, pageNumber);
            countPages = releasesPageable.getPagination().getPages();

            processReleasesAsync(releasesPageable.getReleases(), collectionId, only320);
            log.info("Processed {} page of {}", pageNumber, countPages);

            pageNumber++;
        } while (pageNumber < countPages);
    }


    private void processReleasesAsync(final List<DiscogsReleaseDto> releases, final Long collectionId,
            final boolean only320) {
        for (final DiscogsReleaseDto releaseDto : releases) {
            discogsFacade.getReleaseDto(releaseDto.getId().toString())
                    .ifPresent(release -> processDiscogsReleaseAsync(release, collectionId, only320));
        }
    }


    private void processDiscogsReleaseIdAsync(String releaseId, Long collectionId, boolean only320) {
        discogsFacade.getReleaseDto(releaseId).ifPresent(rel -> processDiscogsReleaseAsync(rel, collectionId, only320));
    }

    //TODO refactoring
    private void processDiscogsReleaseAsync(final DiscogsReleaseResponseDto release, final Long collectionId,
            boolean only320) {
        final Set<String> genres = new HashSet<>();
        if (!isEmpty(release.getStyles())) {
            genres.addAll(release.getStyles());
        }
        //TODO perform search if need for each artist and all combinations of artists
        final String artists = concatArtists(release.getArtists());
        log.debug("Parsed artists: {}", artists);

        processTrackListAsync(release.getTracklist(), artists, genres, collectionId, only320);
    }

    private void processTrackListAsync(final List<DiscogsTrackDto> trackList, final String artists,
            final Set<String> genres, final Long collectionId, boolean only320) {
        int countProcessed = 0;

        if (!isEmpty(trackList)) {
            for (final DiscogsTrackDto discogsTrackDto : trackList) {
                final SearchRequestDto searchRequestDto = getSearchRequestDto(artists, genres, collectionId, discogsTrackDto, only320);

                searchService.searchAndDownloadAsync(searchRequestDto);

                log.debug("Processed {} of {} tracks", ++countProcessed, trackList.size());
            }
        }
    }

    private SearchRequestDto getSearchRequestDto(final String artists, final Set<String> genres,
            final Long collectionId, final DiscogsTrackDto discogsTrackDto, final boolean only320) {
        final String title = discogsTrackDto.getTitle();

        final String artist = discogsTrackDto.getArtists() != null ?
                join(discogsTrackDto.getArtists().stream()
                                .map(discogsArtistDto -> removeEndingNumberInBrackets(discogsArtistDto.getName()))
                                .iterator(),
                        DEFAULT_ARTISTS_DELIMITER) :
                artists;
        log.debug("Artists after parsing track information: {}", artist);

        return createSearchRequestDto(artist, title, genres, collectionId, only320);
    }

    private SearchAndDownloadResponseDto processDiscogsRelease(final DiscogsReleaseResponseDto release,
            final Long collectionId) {
        final Set<String> genres = new HashSet<>(release.getStyles());
        //TODO perform search if need for each artist and all combinations of artists
        final String artists = concatArtists(release.getArtists());
        log.debug("Parsed artists: {}", artists);

        final Map<String, Optional<TrackDto>> tracks = processTrackList(release.getTracklist(), artists, genres, collectionId);

        return toDto(tracks, artists);
    }

    private String concatArtists(final List<DiscogsArtistDto> artists) {
        if (!isEmpty(artists)) {
            return join(artists.stream()
                            .map(discogsArtistDto -> removeEndingNumberInBrackets(discogsArtistDto.getName()))
                            .iterator(),
                    DEFAULT_ARTISTS_DELIMITER);
        } else {
            return "";
        }
    }

    private Map<String, Optional<TrackDto>> processTrackList(final List<DiscogsTrackDto> trackList,
            final String artists,
            final Set<String> genres, final Long collectionId) {
        final Map<String, Optional<TrackDto>> result = new HashMap<>();
        int countProcessed = 0;

        if (!isEmpty(trackList)) {
            for (final DiscogsTrackDto discogsTrackDto : trackList) {
                final SearchRequestDto searchRequestDto = getSearchRequestDto(artists, genres, collectionId, discogsTrackDto, false);

                final Optional<TrackDto> track = searchService.searchAndDownload(searchRequestDto);
                result.put(discogsTrackDto.getTitle(), track);

                log.debug("Processed {} of {} tracks", ++countProcessed, trackList.size());
            }
        }

        return result;
    }

    private SearchRequestDto createSearchRequestDto(final String artists, final String title,
            final Set<String> genres, final Long collectionId, final boolean only320) {
        final SearchRequestDto searchRequestDto = new SearchRequestDto();
        searchRequestDto.setArtists(artists);
        searchRequestDto.setCollectionId(collectionId);
        searchRequestDto.setGenres(genres);
        searchRequestDto.setTitle(title);
        searchRequestDto.setOnly320(only320);
        return searchRequestDto;
    }

    private SearchAndDownloadResponseDto toDto(final Map<String, Optional<TrackDto>> nameToTrackMap,
            final String artists) {
        final SearchAndDownloadResponseDto dto = new SearchAndDownloadResponseDto();
        final List<TrackDto> tracks = new ArrayList<>();
        final List<String> notFound = new ArrayList<>();

        nameToTrackMap.forEach((title, trackOptional) -> {
            if (trackOptional.isPresent()) {
                tracks.add(trackOptional.get());
            } else {
                notFound.add(artists + " - " + title);
            }
        });

        dto.setFound(tracks);
        dto.setNotFound(notFound);
        return dto;
    }

    private List<DiscogsTrackListResponseDto> toTrackListDto(final List<DiscogsTrackDto> trackList,
            final String artistsRelease) {
        final List<DiscogsTrackListResponseDto> result = new ArrayList<>();

        if (!isEmpty(trackList)) {
            trackList.forEach(trackDto -> {
                final DiscogsTrackListResponseDto dto = getTrackListDto(trackDto, artistsRelease);
                result.add(dto);
            });
        }

        return result;
    }

    private DiscogsTrackListResponseDto getTrackListDto(final DiscogsTrackDto trackDto, final String artistsRelease) {
        final DiscogsTrackListResponseDto dto = new DiscogsTrackListResponseDto();

        dto.setTitle(trackDto.getTitle());
        dto.setPosition(trackDto.getPosition());

        final String artists = !isEmpty(trackDto.getArtists()) ? concatArtists(trackDto.getArtists()) : artistsRelease;
        dto.setArtists(artists);

        return dto;
    }

}
