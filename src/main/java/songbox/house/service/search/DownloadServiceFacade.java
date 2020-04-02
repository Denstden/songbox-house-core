package songbox.house.service.search;

import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.TrackDto;
import songbox.house.domain.dto.response.TrackMetadataDto;

import java.util.Optional;

public interface DownloadServiceFacade {
    Optional<TrackDto> download(SearchQueryDto searchQuery);

    Optional<TrackDto> download(TrackMetadataDto trackMetadataDto);
}
