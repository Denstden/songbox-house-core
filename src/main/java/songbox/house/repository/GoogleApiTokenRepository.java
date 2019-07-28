package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.user.GoogleApiToken;

@Repository
public interface GoogleApiTokenRepository extends CrudRepository<GoogleApiToken, Long> {
    GoogleApiToken findByUserId(Long userId);
}
