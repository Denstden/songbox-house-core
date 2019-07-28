package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.TrackListPattern;

@Repository
public interface TrackListPatternRepository extends CrudRepository<TrackListPattern, Long> {
}
