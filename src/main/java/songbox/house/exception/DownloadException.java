package songbox.house.exception;

import lombok.Getter;
import songbox.house.domain.entity.VkAudio;

public class DownloadException extends Exception {

    @Getter
    private final VkAudio audio;

    public DownloadException(final Throwable cause, final VkAudio audio) {
        super(cause);
        this.audio = audio;
    }
}
