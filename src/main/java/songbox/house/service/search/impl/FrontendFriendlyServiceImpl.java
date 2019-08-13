package songbox.house.service.search.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.ArtistTitleDto;
import songbox.house.domain.dto.response.SongDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseDto;
import songbox.house.domain.entity.DiscogsRelease;
import songbox.house.domain.entity.Genre;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.repository.DiscogsReleaseRepository;
import songbox.house.service.*;
import songbox.house.service.search.SearchQuery;
import songbox.house.service.search.SearchServiceFacade;
import songbox.house.util.Pair;
import songbox.house.util.ProgressListener;
import songbox.house.util.compare.ArtistTitleComparator;
import songbox.house.util.compare.SmartDiscogsComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toSet;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Slf4j
public class FrontendFriendlyServiceImpl implements FrontendFriendlyService {
    private static final int DETAILED_INFO_MAX_SONG_SIZE = 2;
    DiscogsFacade discogsFacade;
    DiscogsWebsiteService discogsWebsiteService;
    SearchServiceFacade searchServiceFacade;
    MusicCollectionService musicCollectionService;
    UserService userService;


    GenreService genreService;
    DiscogsReleaseRepository discogsReleaseRepository;

    ArtistTitleComparator artistTitleComparator = new ArtistTitleComparator();


    @Override
    public List<DiscogsReleaseDto> search(String query, boolean fetchResource) {
        log.info("searching for " + query);

        List<DiscogsReleaseDto> search = discogsWebsiteService.search(query);

        log.info("found {} results", search.size());

        return search;
    }

    @Override
    public DiscogsReleaseDto getDetailedInfo(String discogsLink, ProgressListener progressListener) {
        return discogsWebsiteService.getReleaseInfo(discogsLink)
                .map(releaseDto -> getDetailedInfo(releaseDto, progressListener))
                .orElse(null);
    }

    private DiscogsReleaseDto getDetailedInfo(DiscogsReleaseDto releaseInfo, ProgressListener progressListener) {
        // Apply search
        Map<ArtistTitleDto, List<SongDto>> songs = releaseInfo.getSongs();


        AtomicReference<Float> curProgress = new AtomicReference<>((float) 0);
        float progressStep = 1 / (float) (songs.entrySet().size() * 2); // 2 requests
        Runnable invokeProgressListener = () -> {
            if (progressListener != null) {
                progressListener.onProgressChanged(curProgress.updateAndGet(v -> v + progressStep));
            }
        };

        for (Map.Entry<ArtistTitleDto, List<SongDto>> song : songs.entrySet()) {
            ArtistTitleDto artistTitleDto = song.getKey();
            SongDto expectedSongDto = song.getValue().get(0);

            SearchQuery searchQuery = new SearchQuery(String.format("%s %s - %s", releaseInfo.getAudioLabelReleaseName(), artistTitleDto.getArtist(), artistTitleDto.getTitle()));
            searchQuery.setFetchArtwork(true);
            searchQuery.setFilterByArtistTitle(false);

            SearchQuery searchQueryWithoutLabel = new SearchQuery(String.format("%s - %s", artistTitleDto.getArtist(), artistTitleDto.getTitle()));
            searchQueryWithoutLabel.setFetchArtwork(false);
            searchQueryWithoutLabel.setFilterByArtistTitle(false);


            // Apply song side
            Pair<String, String> expectedArtistTitle = Pair.of(artistTitleDto.getArtist(), artistTitleDto.getTitle());
            if (!expectedSongDto.getTrackPos().isEmpty()) {
                expectedArtistTitle = Pair.of(expectedArtistTitle.getLeft(), expectedSongDto.getTrackPos() + " " + expectedArtistTitle.getRight());
            }

            List<SongDto> songSearchResultList = searchServiceFacade.search(searchQuery);
            invokeProgressListener.run();
            songSearchResultList.addAll(searchServiceFacade.search(searchQueryWithoutLabel));
            invokeProgressListener.run();

            songSearchResultList.sort(new SmartDiscogsComparator(expectedSongDto, expectedArtistTitle));

            // Apply track Pos
            songSearchResultList.forEach(e -> e.setTrackPos(expectedSongDto.getTrackPos()));
            songs.put(artistTitleDto, songSearchResultList.subList(0, Math.min(songSearchResultList.size(), DETAILED_INFO_MAX_SONG_SIZE)));
        }
        releaseInfo.setSongs(songs);

        return releaseInfo;
    }

    public DiscogsReleaseDto saveToCollection(DiscogsReleaseDto releaseDto) {
        DiscogsRelease discogsRelease = fromDiscogsReleaseDto(releaseDto);
        return toDiscogsReleaseDto(discogsReleaseRepository.save(discogsRelease));
    }

    @Override
    public DiscogsReleaseDto saveToCollection(String discogsLink) {
        return discogsWebsiteService.getReleaseInfo(discogsLink)
                .map(this::saveToCollection)
                .orElse(null);
    }

    @Override
    public List<DiscogsReleaseDto> getSavedReleases() {
        Iterable<DiscogsRelease> all = discogsReleaseRepository.findAll();
        ArrayList<DiscogsReleaseDto> discogsReleaseDtos = new ArrayList<>();

        for (DiscogsRelease discogsRelease : all) {
            discogsReleaseDtos.add(toDiscogsReleaseDto(discogsRelease));
        }
        return discogsReleaseDtos;
    }

    @Override
    public void deleteFromCollection(Long id) {
        discogsReleaseRepository.deleteById(id);
    }

    private MusicCollection getDefaultMusicCollection() {
        MusicCollection defaultCollection = userService.getUserProperty().getDefaultCollection();
        if (defaultCollection != null) {
            return defaultCollection;
        }
        return musicCollectionService.getOrCreate("frontend-friendly-collection");
    }

    private DiscogsRelease fromDiscogsReleaseDto(DiscogsReleaseDto discogsReleaseDto) {
        DiscogsRelease discogsRelease = new DiscogsRelease();
        discogsRelease.setArtist(discogsReleaseDto.getArtistTitle().getArtist());
        discogsRelease.setTitle(discogsReleaseDto.getArtistTitle().getTitle());
        discogsRelease.setAudioLabel(discogsReleaseDto.getAudioLabel());
        discogsRelease.setAudioLabelReleaseName(discogsReleaseDto.getAudioLabelReleaseName());
        discogsRelease.setCountry(discogsReleaseDto.getCountry());
        discogsRelease.setThumbnail(discogsReleaseDto.getThumbnail());
        discogsRelease.setDiscogsLink(discogsReleaseDto.getDiscogsLink());
        discogsRelease.setGenres(
                discogsReleaseDto.getGenres().stream().map(genreService::getOrCreate).collect(toSet())
        );
        discogsRelease.setCollections(new HashSet<>(Collections.singletonList(getDefaultMusicCollection())));

        return discogsRelease;
    }

    private DiscogsReleaseDto toDiscogsReleaseDto(DiscogsRelease discogsRelease) {
        DiscogsReleaseDto discogsReleaseDto = new DiscogsReleaseDto();

        discogsReleaseDto.setArtistTitle(new ArtistTitleDto(discogsRelease.getArtist(), discogsRelease.getTitle()));
        discogsReleaseDto.setAudioLabel(discogsRelease.getAudioLabel());
        discogsReleaseDto.setAudioLabelReleaseName(discogsRelease.getAudioLabelReleaseName());
        discogsReleaseDto.setCountry(discogsRelease.getCountry());
        discogsReleaseDto.setThumbnail(discogsRelease.getThumbnail());
        discogsReleaseDto.setDiscogsLink(discogsRelease.getDiscogsLink());
        discogsReleaseDto.setGenres(
                discogsRelease.getGenres().stream().map(Genre::getName).collect(toSet())
        );
        return discogsReleaseDto;
    }

}
