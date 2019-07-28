package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.Author;

@Repository
public interface AuthorRepository extends CrudRepository<Author, Long> {
    Author findByName(final String name);
}
