package songbox.house.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import songbox.house.event.SearchReprocessFoundEvent;

@Component
@Slf4j
public class LoggingSearchReprocessFoundEventListener implements ApplicationListener<SearchReprocessFoundEvent> {
    @Override
    public void onApplicationEvent(SearchReprocessFoundEvent searchReprocessFoundEvent) {
        log.info("Received event: {}", searchReprocessFoundEvent);
    }
}
