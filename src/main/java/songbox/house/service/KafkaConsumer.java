package songbox.house.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;

@Data
@Slf4j
public class KafkaConsumer {

    public static final String FAILED_QUERIES_TOPIC_NAME = "failed_queries";

    @KafkaListener(topics = FAILED_QUERIES_TOPIC_NAME)
    public void listen(@Payload String message) {
        log.trace("Consumed '{}' from {} topic", message, FAILED_QUERIES_TOPIC_NAME);
        // TODO fix duplicates
        // TODO decide what to do (sleep/retry(how much?)/some smarter)
    }
}
