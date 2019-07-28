package songbox.house.domain.event.vk;

import org.springframework.context.ApplicationEvent;

public abstract class DownloadEvent extends ApplicationEvent {
    DownloadEvent(Object source) {
        super(source);
    }
}
