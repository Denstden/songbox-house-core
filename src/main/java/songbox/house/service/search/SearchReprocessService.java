package songbox.house.service.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import songbox.house.domain.entity.SearchReprocess;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.runAsync;

public interface SearchReprocessService {
    SearchReprocess createIfNotExists(String searchQuery, Long collectionId, Set<String> genres, Long userId);

    Page<SearchReprocess> allForCurrentUser(Pageable pageable);

    Page<SearchReprocess> availableForSearch(Long userId, Pageable pageable);

    Page<SearchReprocess> availableForDownloading(Long userId, Pageable pageable);

    Page<SearchReprocess> downloaded(Long userId, Pageable pageable);

    void download(Long userId, Set<Long> searchReprocessIds);

    void downloadAll(Long userId);

    default CompletableFuture<Void> reprocessAllUsersAsync() {
        return runAsync(this::reprocessAllUsers);
    }

    void reprocessAllUsers();

    void reprocess(Long userId);
}
