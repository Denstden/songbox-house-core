package songbox.house.service.search;

import songbox.house.domain.dto.response.SongDto;

import java.util.List;

public interface SearchServiceFacade {
    List<SongDto> search(SearchQuery query);
}
