package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.Genre;

@Repository
public interface GenreRepository extends CrudRepository<Genre, Long> {
    Genre findByName(final String name);
}
