package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.YoutubePlaylist;

import java.util.Collection;
import java.util.List;

@Repository
public interface YoutubePlaylistRepository extends CrudRepository<YoutubePlaylist, Long> {
    YoutubePlaylist findByYoutubeId(String youtubeId);

    List<YoutubePlaylist> findByYoutubeIdIn(Collection<String> youtubeIds);
}
