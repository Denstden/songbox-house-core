package songbox.house.service.search.vk;

import org.springframework.context.event.EventListener;
import songbox.house.domain.entity.VkAudio;
import songbox.house.domain.event.vk.VkDownloadStatusEvent;

public interface VkAudioService {

    void save(VkAudio audio);

    @EventListener
    void onDownloadStatusEvent(VkDownloadStatusEvent event);

}
