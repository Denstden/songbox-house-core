package songbox.house.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import songbox.house.service.KafkaService;

import static songbox.house.service.KafkaConsumer.FAILED_QUERIES_TOPIC_NAME;

@Slf4j
@Data
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void sendToFailedQueries(String query) {
        kafkaTemplate.send(FAILED_QUERIES_TOPIC_NAME, query);
    }
}
