package songbox.house.repository;

import songbox.house.domain.dto.SearchReprocessResultDto;

import java.util.Map;
import java.util.Set;

public interface SearchReprocessResultRepository {
    void save(Long userId, Map<Long, SearchReprocessResultDto> reprocessResult);

    Map<Long, SearchReprocessResultDto> get(Long userId, Set<Long> searchReprocessIds);

    Map<Long, SearchReprocessResultDto> available(Long userId);

    void remove(Long userId, Set<Long> downloadedReprocessIds);
}
