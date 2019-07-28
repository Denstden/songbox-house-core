package songbox.house.domain.event.vk;

import songbox.house.domain.DownloadStatus;
import songbox.house.domain.entity.VkAudio;

public abstract class VkDownloadStatusEvent extends DownloadEvent {
    VkDownloadStatusEvent(final Object source) {
        super(source);
    }

    public abstract VkAudio getAudio();

    public abstract DownloadStatus getStatus();
}
