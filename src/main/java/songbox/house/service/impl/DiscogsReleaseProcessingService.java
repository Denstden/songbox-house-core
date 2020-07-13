package songbox.house.service.impl;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.SearchRequestDto;
import songbox.house.domain.dto.response.SearchAndDownloadResponseDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseResponseDto;
import songbox.house.domain.dto.response.discogs.DiscogsTrackDto;
import songbox.house.service.search.TrackDownloadService;
import songbox.house.util.DiscogsArtistsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Service
class DiscogsReleaseProcessingService {

    TrackDownloadService searchService;

    SearchAndDownloadResponseDto processDiscogsRelease(DiscogsReleaseResponseDto release,
            Long collectionId, boolean only320) {
        Set<String> genres = new HashSet<>(release.getStyles());
        //TODO perform search if need for each artist and all combinations of artists
        String artists = DiscogsArtistsUtil.concatArtists(release.getArtists());
        log.debug("Parsed artists: {}", artists);

        Map<String, Optional<TrackDto>> tracks = processTrackList(release.getTracklist(), artists, genres,
                collectionId, only320);

        return toDto(tracks, artists);
    }

    private Map<String, Optional<TrackDto>> processTrackList(List<DiscogsTrackDto> trackList,
            String artists, Set<String> genres, Long collectionId, boolean only320) {

        Map<String, Optional<TrackDto>> result = new HashMap<>();
        int countProcessed = 0;

        if (!isEmpty(trackList)) {
            for (DiscogsTrackDto discogsTrackDto : trackList) {
                Optional<TrackDto> track = getTrackDto(artists, genres, collectionId, only320, discogsTrackDto);
                result.put(discogsTrackDto.getTitle(), track);

                log.debug("Processed {} of {} tracks", ++countProcessed, trackList.size());
            }
        }

        return result;
    }

    private Optional<TrackDto> getTrackDto(String artists, Set<String> genres, Long collectionId, boolean only320,
            DiscogsTrackDto discogsTrackDto) {
        SearchRequestDto searchRequestDto = getSearchRequestDto(artists, genres, collectionId, discogsTrackDto, only320);

        return searchService.searchAndDownload(searchRequestDto);
    }

    private SearchRequestDto getSearchRequestDto(String artists, Set<String> genres,
            Long collectionId, DiscogsTrackDto discogsTrackDto, boolean only320) {
        String title = discogsTrackDto.getTitle();

        String artist = ofNullable(discogsTrackDto.getArtists())
                .map(DiscogsArtistsUtil::concatArtists)
                .orElse(artists);
        log.debug("Artists after parsing track information: {}", artist);

        return createSearchRequestDto(artist, title, genres, collectionId, only320);
    }

    private SearchRequestDto createSearchRequestDto(String artists, String title,
            Set<String> genres, Long collectionId, boolean only320) {
        SearchRequestDto searchRequestDto = new SearchRequestDto();
        searchRequestDto.setArtists(artists);
        searchRequestDto.setCollectionId(collectionId);
        searchRequestDto.setGenres(genres);
        searchRequestDto.setTitle(title);
        searchRequestDto.setOnly320(only320);
        return searchRequestDto;
    }

    private SearchAndDownloadResponseDto toDto(Map<String, Optional<TrackDto>> nameToTrackMap,
            String artists) {
        SearchAndDownloadResponseDto dto = new SearchAndDownloadResponseDto();
        List<TrackDto> tracks = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

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
}
