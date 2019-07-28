package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.MusicCollection;

@Repository
public interface MusicCollectionRepository extends CrudRepository<MusicCollection, Long> {

    MusicCollection findByCollectionNameIgnoreCase(final String collectionName);

    Iterable<MusicCollection> findByOwner_UserName(final String userName);
}
