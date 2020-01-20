package songbox.house.service.search.impl;

import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.dto.response.TrackMetadataDto;
import songbox.house.service.download.DownloadService;
import songbox.house.service.search.DownloadServiceFacade;

import java.util.List;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Service
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Slf4j
public class DownloadServiceFacadeImpl implements DownloadServiceFacade {

    List<DownloadService> downloadServices;

    @Autowired
    public DownloadServiceFacadeImpl(List<DownloadService> downloadServices) {
        this.downloadServices = downloadServices;
    }

    @Override
    public Optional<TrackDto> download(SearchQueryDto searchQuery) {
        return downloadServices.stream()
                .filter(DownloadService::isDownloadEnabled)
                .findFirst()
                .flatMap(downloadService -> downloadService.download(searchQuery));
    }

    @Override
    public Optional<TrackDto> download(TrackMetadataDto trackMetadataDto) {
        return downloadServices.stream()
                .filter(DownloadService::isDownloadEnabled)
                .findFirst()
                .flatMap(downloadService -> downloadService.download(trackMetadataDto));
    }

}
