package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.user.UserRole;
import songbox.house.domain.entity.user.UserRole.RoleName;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, Long> {
    Optional<UserRole> findByName(RoleName name);
}
