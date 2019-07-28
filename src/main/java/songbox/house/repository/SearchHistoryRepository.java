package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.SearchStatus;
import songbox.house.domain.entity.SearchHistory;

@Repository
public interface SearchHistoryRepository extends CrudRepository<SearchHistory, Long> {
    Iterable<SearchHistory> findBySearchStatus(SearchStatus searchStatus);
}
