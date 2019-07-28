package songbox.house.service.search.vk;

import songbox.house.domain.entity.SearchHistory;
import songbox.house.domain.entity.Track;

import java.util.Optional;
import java.util.Set;

public interface VkSearchDownloadService {
    Optional<Track> searchAndDownload(final String authors, final String title, final Set<String> genres,
            final Long collectionId, final SearchHistory searchHistory);

    void searchAndDownloadAsync(final String authors, final String title, final Set<String> genres,
            final Long collectionId, final SearchHistory searchHistory, boolean only320);
}
