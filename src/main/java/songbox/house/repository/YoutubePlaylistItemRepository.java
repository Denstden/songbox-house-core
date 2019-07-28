package songbox.house.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import songbox.house.domain.entity.YoutubePlaylistItem;

import java.util.Collection;
import java.util.List;

@Repository
public interface YoutubePlaylistItemRepository extends CrudRepository<YoutubePlaylistItem, Long> {
    List<YoutubePlaylistItem> findByYoutubeVideoIdIn(Collection<String> youtubeVideoIds);
}
