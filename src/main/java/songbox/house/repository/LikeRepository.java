package songbox.house.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
}
