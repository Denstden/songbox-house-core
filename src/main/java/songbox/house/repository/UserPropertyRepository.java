package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.user.UserProperty;

import java.util.Optional;

@Repository
public interface UserPropertyRepository extends CrudRepository<UserProperty, Long> {
}