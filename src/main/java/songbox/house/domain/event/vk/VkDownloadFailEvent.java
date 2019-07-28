package songbox.house.domain.event.vk;

import lombok.Getter;
import songbox.house.domain.DownloadStatus;
import songbox.house.domain.entity.VkAudio;
import songbox.house.exception.DownloadException;

public class VkDownloadFailEvent extends VkDownloadStatusEvent {

    @Getter
    private final DownloadException cause;

    public VkDownloadFailEvent(final Object source, final DownloadException cause) {
        super(source);
        this.cause = cause;
    }

    @Override
    public VkAudio getAudio() {
        return cause.getAudio();
    }

    @Override
    public DownloadStatus getStatus() {
        return DownloadStatus.FAIL;
    }
}
