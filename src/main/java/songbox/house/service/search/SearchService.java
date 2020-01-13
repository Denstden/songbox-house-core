package songbox.house.service.search;

import songbox.house.domain.dto.response.TrackMetadataDto;

import java.util.List;

public interface SearchService {
    List<TrackMetadataDto> search(SearchQueryDto query);

    String resourceName();
}
