package songbox.house.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import songbox.house.service.KafkaService;

@Service
@Data
@Slf4j
public class KafkaServiceImpl implements KafkaService {

    public static final String FAILED_QUERIES_TOPIC_NAME = "failed_queries";

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void sendToFailedQueries(String query) {
        kafkaTemplate.send(FAILED_QUERIES_TOPIC_NAME, query);
    }

    @KafkaListener(topics = FAILED_QUERIES_TOPIC_NAME)
    public void listen(@Payload String message) {
        log.trace("Consumed '{}' from {} topic", message, FAILED_QUERIES_TOPIC_NAME);
        // TODO fix duplicates
        // TODO decide what to do (sleep/retry(how much?)/some smarter)
    }
}
