package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.user.UserInfo;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends CrudRepository<UserInfo, Long> {
    Optional<UserInfo> findByUserName(String userName);
}
