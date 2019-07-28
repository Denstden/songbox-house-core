package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.DiscogsRelease;


@Repository
public interface DiscogsReleaseRepository extends CrudRepository<DiscogsRelease, Long> {
}
