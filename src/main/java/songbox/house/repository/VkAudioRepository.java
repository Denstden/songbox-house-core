package songbox.house.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.VkAudio;

@Repository
public interface VkAudioRepository extends CrudRepository<VkAudio, Long> {
}
