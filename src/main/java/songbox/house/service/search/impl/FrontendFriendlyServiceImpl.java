package songbox.house.service.search.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.ArtistTitleDto;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.SongDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.domain.dto.response.discogs.DiscogsReleaseDtoExt;
import songbox.house.domain.entity.DiscogsRelease;
import songbox.house.domain.entity.Genre;
import songbox.house.domain.entity.MusicCollection;
import songbox.house.repository.DiscogsReleaseRepository;
import songbox.house.service.DiscogsFacade;
import songbox.house.service.DiscogsWebsiteService;
import songbox.house.service.FrontendFriendlyService;
import songbox.house.service.GenreService;
import songbox.house.service.MusicCollectionService;
import songbox.house.service.UserPropertyService;
import songbox.house.service.search.SearchServiceFacade;
import songbox.house.util.ArtistsTitle;
import songbox.house.util.ProgressListener;
import songbox.house.util.compare.ArtistTitleComparator;
import songbox.house.util.compare.SmartDiscogsComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    UserPropertyService userPropertyService;


    GenreService genreService;
    DiscogsReleaseRepository discogsReleaseRepository;

    ArtistTitleComparator artistTitleComparator = new ArtistTitleComparator();


    @Override
    public List<DiscogsReleaseDtoExt> search(String query, boolean fetchResource) {
        log.info("searching for " + query);

        List<DiscogsReleaseDtoExt> search = discogsWebsiteService.search(query);

        log.info("found {} results", search.size());

        return search;
    }

    @Override
    public DiscogsReleaseDtoExt getDetailedInfo(String discogsLink, ProgressListener progressListener,
            boolean useFastSearch) {

        return discogsWebsiteService.getReleaseInfo(discogsLink)
                .map(releaseDto -> getDetailedInfo(releaseDto, progressListener, useFastSearch))
                .orElse(null);
    }

    private DiscogsReleaseDtoExt getDetailedInfo(DiscogsReleaseDtoExt releaseInfo, ProgressListener progressListener,
            boolean useFastSearch) {
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

            SearchQueryDto searchQueryDto = new SearchQueryDto(String.format("%s %s - %s", releaseInfo.getAudioLabelReleaseName(), artistTitleDto.getArtist(), artistTitleDto.getTitle()));
            searchQueryDto.setFetchArtwork(true);
            searchQueryDto.setFilterByArtistTitle(false);

            SearchQueryDto searchQueryDtoWithoutLabel = new SearchQueryDto(String.format("%s - %s", artistTitleDto.getArtist(), artistTitleDto.getTitle()));
            searchQueryDtoWithoutLabel.setFetchArtwork(false);
            searchQueryDtoWithoutLabel.setFilterByArtistTitle(false);


            // Apply song side
            ArtistsTitle expectedArtistTitle = ArtistsTitle.of(artistTitleDto.getArtist(), artistTitleDto.getTitle());
            if (!expectedSongDto.getTrackPos().isEmpty()) {
                expectedArtistTitle = ArtistsTitle.of(expectedArtistTitle.getArtists(), expectedSongDto.getTrackPos() + " " + expectedArtistTitle.getTitle());
            }

            List<TrackMetadataDto> songSearchResultList = searchServiceFacade.search(searchQueryDto, useFastSearch);
            invokeProgressListener.run();
            songSearchResultList.addAll(searchServiceFacade.search(searchQueryDtoWithoutLabel, useFastSearch));
            invokeProgressListener.run();

            songSearchResultList.sort(new SmartDiscogsComparator(expectedSongDto, expectedArtistTitle));

            // Apply track Pos
            List<SongDto> songDTOs = songSearchResultList.stream()
                    .map(trackMetadataDto -> toSongDto(trackMetadataDto, expectedSongDto.getTrackPos()))
                    .collect(Collectors.toList());
            songs.put(artistTitleDto, songDTOs.subList(0, Math.min(songDTOs.size(), DETAILED_INFO_MAX_SONG_SIZE)));
        }
        releaseInfo.setSongs(songs);

        return releaseInfo;
    }

    private SongDto toSongDto(TrackMetadataDto trackMetadataDto, String trackPos) {
        SongDto songDto = new SongDto();
        songDto.setArtist(trackMetadataDto.getArtists());
        songDto.setTitle(trackMetadataDto.getTitle());
        songDto.setBitRate(trackMetadataDto.getBitRate());
        songDto.setDuration(trackMetadataDto.getDurationSec());
        songDto.setResource(trackMetadataDto.getResource());
        //TODO thumbnail is null ??
        songDto.setThumbnail(trackMetadataDto.getThumbnail());
        songDto.setUri(trackMetadataDto.getUri());
        songDto.setTrackPos(trackPos);
        return songDto;
    }

    private DiscogsReleaseDtoExt saveToCollection(DiscogsReleaseDtoExt releaseDto) {
        DiscogsRelease discogsRelease = fromDiscogsReleaseDto(releaseDto);
        return toDiscogsReleaseDto(discogsReleaseRepository.save(discogsRelease));
    }

    @Override
    public DiscogsReleaseDtoExt saveToCollection(String discogsLink) {
        return discogsWebsiteService.getReleaseInfo(discogsLink)
                .map(this::saveToCollection)
                .orElse(null);
    }

    @Override
    public List<DiscogsReleaseDtoExt> getSavedReleases() {
        Iterable<DiscogsRelease> all = discogsReleaseRepository.findAll();
        ArrayList<DiscogsReleaseDtoExt> discogsReleaseDtoExts = new ArrayList<>();

        for (DiscogsRelease discogsRelease : all) {
            discogsReleaseDtoExts.add(toDiscogsReleaseDto(discogsRelease));
        }
        return discogsReleaseDtoExts;
    }

    @Override
    public void deleteFromCollection(Long id) {
        discogsReleaseRepository.deleteById(id);
    }

    private MusicCollection getDefaultMusicCollection() {
        MusicCollection defaultCollection = userPropertyService.getCurrentUserProperty().getDefaultCollection();
        if (defaultCollection != null) {
            return defaultCollection;
        }
        return musicCollectionService.getOrCreate("frontend-friendly-collection");
    }

    private DiscogsRelease fromDiscogsReleaseDto(DiscogsReleaseDtoExt discogsReleaseDtoExt) {
        DiscogsRelease discogsRelease = new DiscogsRelease();
        discogsRelease.setArtist(discogsReleaseDtoExt.getArtistTitle().getArtist());
        discogsRelease.setTitle(discogsReleaseDtoExt.getArtistTitle().getTitle());
        discogsRelease.setAudioLabel(discogsReleaseDtoExt.getAudioLabel());
        discogsRelease.setAudioLabelReleaseName(discogsReleaseDtoExt.getAudioLabelReleaseName());
        discogsRelease.setCountry(discogsReleaseDtoExt.getCountry());
        discogsRelease.setThumbnail(discogsReleaseDtoExt.getThumbnail());
        discogsRelease.setDiscogsLink(discogsReleaseDtoExt.getDiscogsLink());
        discogsRelease.setGenres(
                discogsReleaseDtoExt.getGenres().stream().map(genreService::getOrCreate).collect(toSet())
        );
        discogsRelease.setCollections(new HashSet<>(Collections.singletonList(getDefaultMusicCollection())));

        return discogsRelease;
    }

    private DiscogsReleaseDtoExt toDiscogsReleaseDto(DiscogsRelease discogsRelease) {
        DiscogsReleaseDtoExt discogsReleaseDtoExt = new DiscogsReleaseDtoExt();

        discogsReleaseDtoExt.setArtistTitle(new ArtistTitleDto(discogsRelease.getArtist(), discogsRelease.getTitle()));
        discogsReleaseDtoExt.setAudioLabel(discogsRelease.getAudioLabel());
        discogsReleaseDtoExt.setAudioLabelReleaseName(discogsRelease.getAudioLabelReleaseName());
        discogsReleaseDtoExt.setCountry(discogsRelease.getCountry());
        discogsReleaseDtoExt.setThumbnail(discogsRelease.getThumbnail());
        discogsReleaseDtoExt.setDiscogsLink(discogsRelease.getDiscogsLink());
        discogsReleaseDtoExt.setGenres(
                discogsRelease.getGenres().stream().map(Genre::getName).collect(toSet())
        );
        return discogsReleaseDtoExt;
    }

}
