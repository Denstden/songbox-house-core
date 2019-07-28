package songbox.house.service.search.vk;

import songbox.house.domain.entity.SearchHistory;
import songbox.house.domain.entity.VkAudio;
import songbox.house.service.search.SearchService;

import java.util.Optional;

public interface VkSearchService extends SearchService {
    Optional<VkAudio> searchForDownloading(final String authors, final String title, final SearchHistory searchHistory,
            final boolean only320);

    default String resourceName() {
        return "VK";
    }
}
