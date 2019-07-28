package songbox.house.service.search.vk;

import songbox.house.domain.dto.response.vk.VkSearchResponseDto;
import songbox.house.service.search.SearchQuery;

import java.util.List;

public interface VkSearchPerformer {
    List<VkSearchResponseDto> performSearch(final SearchQuery searchQuery);
}
