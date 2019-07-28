package songbox.house.service;

public interface KafkaService {
    void sendToFailedQueries(String query);
}
