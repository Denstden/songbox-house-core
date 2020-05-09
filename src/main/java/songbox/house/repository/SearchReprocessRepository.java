package songbox.house.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.SearchReprocess;
import songbox.house.domain.entity.SearchReprocessStatus;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SearchReprocessRepository extends CrudRepository<SearchReprocess, Long> {
    @Query(value = "select distinct s.userId from SearchReprocess s where s.status = ?1")
    Iterable<Long> findAllUsersForReprocess(SearchReprocessStatus status);

    Page<SearchReprocess> findByUserId(Long userId, Pageable pageable);

    Optional<SearchReprocess> findByUserIdAndSearchQuery(Long userId, String searchQuery);

    Page<SearchReprocess> findByUserIdAndStatus(Long userId, SearchReprocessStatus status, Pageable pageable);

    @Modifying
    @Query("update SearchReprocess u set u.status = songbox.house.domain.entity.SearchReprocessStatus.DOWNLOADED, " +
            "u.downloadedAt = :downloadedAt where u.id in (:ids)")
    void setDownloaded(@Param("downloadedAt") Date downloadedAt, @Param("ids") Set<Long> ids);

    @Modifying
    @Query("update SearchReprocess u set u.status = songbox.house.domain.entity.SearchReprocessStatus.FOUND, u.foundAt = :foundAt where u.id in (:ids)")
    void setFound(@Param("foundAt") Date foundAt, @Param("ids") List<Long> ids);

    @Modifying
    @Query("update SearchReprocess u set u.status = songbox.house.domain.entity.SearchReprocessStatus.NOT_FOUND, " +
            "u.foundAt = null " +
            "where u.status = songbox.house.domain.entity.SearchReprocessStatus.FOUND and u.id not in (:ids)")
    void setNotFound(@Param("ids") Set<Long> foundIds);

    @Modifying
    @Query("update SearchReprocess u set u.status = songbox.house.domain.entity.SearchReprocessStatus.NOT_FOUND, " +
            "u.foundAt = null " +
            "where u.status = songbox.house.domain.entity.SearchReprocessStatus.FOUND and u.userId = :userId")
    void setNotFound(@Param("userId") Long userId);

    @Modifying
    @Query("update SearchReprocess u set u.status = songbox.house.domain.entity.SearchReprocessStatus.NOT_FOUND, " +
            "u.foundAt = null " +
            "where u.id = :id")
    void setNotFoundById(@Param("id") Long id);

    @Modifying
    @Query("update SearchReprocess u set u.retries = u.retries + 1 where u.id in (:ids)")
    void incrementRetryCount(@Param("ids") Set<Long> ids);
}
