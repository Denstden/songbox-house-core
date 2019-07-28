package songbox.house.domain.event.vk;

import lombok.Getter;
import songbox.house.domain.entity.VkAudio;

public class VkDownloadFinishEvent extends DownloadEvent {

    @Getter
    private final VkAudio track;

    public VkDownloadFinishEvent(final Object source, final VkAudio track) {
        super(source);
        this.track = track;
    }

}
