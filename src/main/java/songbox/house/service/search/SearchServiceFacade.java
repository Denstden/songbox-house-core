package songbox.house.service.search;

import songbox.house.domain.dto.response.TrackMetadataDto;

import java.util.List;

public interface SearchServiceFacade {
    List<TrackMetadataDto> search(SearchQueryDto query);
}
