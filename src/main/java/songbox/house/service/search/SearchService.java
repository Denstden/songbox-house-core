package songbox.house.service.search;

import songbox.house.domain.dto.response.SongDto;

import java.util.List;

public interface SearchService {
    List<SongDto> search(SearchQuery query) throws Exception;

    String resourceName();
}
