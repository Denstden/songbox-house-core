package songbox.house.service.search.impl;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.service.DiscogsWebsiteService;
import songbox.house.service.download.DownloadService;
import songbox.house.service.search.DownloadServiceFacade;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Slf4j
public class DownloadServiceFacadeImpl implements DownloadServiceFacade {

    List<DownloadService> downloadServices;
    DiscogsWebsiteService discogsWebsiteService;

    @Autowired
    public DownloadServiceFacadeImpl(List<DownloadService> downloadServices,
            DiscogsWebsiteService discogsWebsiteService) {
        this.downloadServices = downloadServices;
        this.discogsWebsiteService = discogsWebsiteService;
    }

    @Override
    public Optional<TrackDto> download(SearchQueryDto searchQuery) {
        final CompletableFuture<Optional<String>> artworksFuture = searchQuery.isFetchArtwork()
                ? supplyAsync(() -> discogsWebsiteService.searchArtwork(searchQuery.getQuery()))
                : null;

        final Optional<TrackDto> track = downloadServices.stream()
                .filter(DownloadService::isDownloadEnabled)
                .findFirst()
                .flatMap(downloadService -> downloadService.download(searchQuery));

        track.ifPresent(trackDto -> getArtworkUrl(artworksFuture).ifPresent(trackDto::setArtworkUrl));

        return track;
    }

    @Override
    public Optional<TrackDto> download(TrackMetadataDto trackMetadataDto) {
        searchArtworkIfNeed(trackMetadataDto);

        return downloadServices.stream()
                .filter(DownloadService::isDownloadEnabled)
                .findFirst()
                .flatMap(downloadService -> downloadService.download(trackMetadataDto));
    }

    private void searchArtworkIfNeed(TrackMetadataDto trackMetadataDto) {
        if (isNotBlank(trackMetadataDto.getThumbnail())) {
            final String searchQuery = trackMetadataDto.getArtists() + trackMetadataDto.getTitle();
            final CompletableFuture<Optional<String>> artworksFuture = supplyAsync(() ->
                    discogsWebsiteService.searchArtwork(searchQuery));
            getArtworkUrl(artworksFuture).ifPresent(trackMetadataDto::setThumbnail);
        }
    }

    private Optional<String> getArtworkUrl(CompletableFuture<Optional<String>> artworksFuture) {
        return ofNullable(artworksFuture).flatMap(CompletableFuture::join);
    }

}
