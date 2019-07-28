package songbox.house.service.search.vk;

import songbox.house.domain.entity.Track;
import songbox.house.domain.entity.VkAudio;

import java.util.Optional;
import java.util.Set;

public interface VkDownloadService {

    Optional<Track> download(VkAudio vkAudio, Set<String> genres, Long collectionId);

    void downloadAsync(VkAudio audio, Set<String> genres, Long collectionId);

}
