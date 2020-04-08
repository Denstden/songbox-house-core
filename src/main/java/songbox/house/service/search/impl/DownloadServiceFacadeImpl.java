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

import static java.util.Comparator.comparingInt;
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
        final String artworkUrl = discogsWebsiteService.searchArtwork(searchQuery.getQuery()).orElse(null);

        return downloadServices.stream()
                .max(comparingInt(DownloadService::getDownloadPriority))
                .flatMap(downloadService -> downloadService.download(searchQuery, artworkUrl));
    }

    @Override
    public Optional<TrackDto> download(TrackMetadataDto trackMetadataDto) {
        searchArtworkIfNeed(trackMetadataDto);
        String serviceName = trackMetadataDto.getUri().split(":")[0];

        return downloadServices.stream()
                .filter(service -> service.canDownload(serviceName))
                .max(comparingInt(DownloadService::getDownloadPriority))
                .flatMap(downloadService -> downloadService.download(trackMetadataDto));
    }

    private void searchArtworkIfNeed(TrackMetadataDto trackMetadataDto) {
        if (isNotBlank(trackMetadataDto.getThumbnail())) {
            final String searchQuery = trackMetadataDto.getArtistsTitle().toString();
            discogsWebsiteService.searchArtwork(searchQuery).ifPresent(trackMetadataDto::setThumbnail);
        }
    }
}
