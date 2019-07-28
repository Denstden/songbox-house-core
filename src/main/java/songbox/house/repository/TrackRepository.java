package songbox.house.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.Track;

@Repository
public interface TrackRepository extends PagingAndSortingRepository<Track, Long> {
    Page<Track> findByCollections_CollectionId(final Long collectionId, final Pageable page);

    Track findFirstByAuthorsStrIgnoreCaseAndTitleIgnoreCase(final String artist, final String title);

    int deleteByCollections_CollectionId(final Long collectionId);
}
