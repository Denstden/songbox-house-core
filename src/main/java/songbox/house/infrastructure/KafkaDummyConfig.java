package songbox.house.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import songbox.house.service.KafkaService;

@ConditionalOnProperty(name = "kafka.enabled", havingValue = "false")
@Configuration
public class KafkaDummyConfig {
    @Bean
    public KafkaService kafkaService() {
        return query -> {
        };
    }
}
