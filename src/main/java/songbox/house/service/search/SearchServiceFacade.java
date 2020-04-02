package songbox.house.service.search;

import songbox.house.domain.dto.request.SearchQueryDto;
import songbox.house.domain.dto.response.SongDto;
import songbox.house.domain.dto.response.TrackMetadataDto;

import java.util.List;

public interface SearchServiceFacade {
    List<TrackMetadataDto> search(SearchQueryDto query);

    List<TrackMetadataDto> searchFast(SearchQueryDto query);

    default List<TrackMetadataDto> search(SearchQueryDto query, boolean isFast) {
        return isFast ? searchFast(query) : search(query);
    }
}
