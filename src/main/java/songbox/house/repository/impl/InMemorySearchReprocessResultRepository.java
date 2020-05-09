package songbox.house.repository.impl;

import org.springframework.stereotype.Repository;
import songbox.house.domain.dto.SearchReprocessResultDto;
import songbox.house.repository.SearchReprocessResultRepository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

@Repository
public class InMemorySearchReprocessResultRepository implements SearchReprocessResultRepository {

    private static final Map<Long, Map<Long, SearchReprocessResultDto>> CACHE = new ConcurrentHashMap<>();

    @Override
    public void save(Long userId, Map<Long, SearchReprocessResultDto> reprocessResult) {
        final Map<Long, SearchReprocessResultDto> byUser = CACHE.get(userId);
        if (isNull(byUser)) {
            CACHE.put(userId, new ConcurrentHashMap<>(reprocessResult));
        } else {
            byUser.putAll(reprocessResult);
        }
    }

    @Override
    public Map<Long, SearchReprocessResultDto> get(Long userId, Set<Long> searchReprocessIds) {
        final Map<Long, SearchReprocessResultDto> byUser = CACHE.get(userId);
        if (isNull(byUser)) {
            return emptyMap();
        } else {
            return byUser.entrySet().stream()
                    .filter(entry -> searchReprocessIds.contains(entry.getKey()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    @Override
    public Map<Long, SearchReprocessResultDto> available(Long userId) {
        final Map<Long, SearchReprocessResultDto> byUser = CACHE.get(userId);
        if (isNull(byUser)) {
            return emptyMap();
        } else {
            return byUser;
        }
    }

    @Override
    public void remove(Long userId, Set<Long> downloadedReprocessIds) {
        final Map<Long, SearchReprocessResultDto> byUser = CACHE.get(userId);
        if (nonNull(byUser)) {
            downloadedReprocessIds.forEach(byUser::remove);
        }
    }
}
