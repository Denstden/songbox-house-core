package songbox.house.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import songbox.house.domain.dto.SearchReprocessResultDto;
import songbox.house.infrastructure.redis.RedisDAO;
import songbox.house.infrastructure.redis.RedisDAOImpl;
import songbox.house.repository.SearchReprocessResultRepository;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static java.util.stream.Collectors.toSet;

@Component
@Slf4j
@ConditionalOnProperty(name = "songbox.house.search.reprocess.redis", matchIfMissing = true, havingValue = "true")
public class RedisSearchReprocessResultRepository implements SearchReprocessResultRepository {

    private final RedisDAO redisDAO;
    private final ObjectMapper objectMapper;

    public RedisSearchReprocessResultRepository(
            @Value("${songbox.house.search.reprocess.result.redis.host:localhost}") String host,
            @Value("${songbox.house.search.reprocess.result.redis.port:36379}") Integer port,
            @Value("${songbox.house.search.reprocess.result.redis.password:d0cker}") String password,
            @Value("${songbox.house.search.reprocess.result.redis.db.index:1}") Integer dbIndex,
            @Value("${songbox.house.search.reprocess.result.redis.timeout.ms:2000}") Integer timeoutMs) {
        this.redisDAO = new RedisDAOImpl(host, port, password, dbIndex, timeoutMs);
        this.objectMapper = new ObjectMapper()
                .disable(FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public void save(Long userId, Map<Long, SearchReprocessResultDto> reprocessResult) {
        String key = getKey(userId);
        redisDAO.doInPipeline(pipeline -> {
            reprocessResult.forEach((k, v) -> pipeline.hset(key, String.valueOf(k), serialize(v)));
        });
    }

    @Override
    public Map<Long, SearchReprocessResultDto> get(Long userId, Set<Long> searchReprocessIds) {
        String key = getKey(userId);
        return redisDAO.hgetAll(key).entrySet().stream()
                .filter(entry -> searchReprocessIds.contains(Long.valueOf(entry.getKey())))
                .collect(Collectors.toMap(e -> Long.valueOf(e.getKey()), e -> deserialize(e.getValue())));
    }

    @Override
    public Map<Long, SearchReprocessResultDto> available(Long userId) {
        String key = getKey(userId);
        return redisDAO.hgetAll(key).entrySet().stream()
                .collect(Collectors.toMap(e -> Long.valueOf(e.getKey()), e -> deserialize(e.getValue())));
    }

    @Override
    public void remove(Long userId, Set<Long> downloadedReprocessIds) {
        String key = getKey(userId);
        redisDAO.hdel(key, downloadedReprocessIds.stream().map(String::valueOf).collect(toSet()));
    }

    private String getKey(Long userId) {
        return String.valueOf(userId);
    }

    private String serialize(SearchReprocessResultDto searchReprocessResult) {
        try {
            return objectMapper.writeValueAsString(searchReprocessResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private SearchReprocessResultDto deserialize(String searchReprocessResult) {
        try {
            return objectMapper.readValue(searchReprocessResult, SearchReprocessResultDto.class);
        } catch (IOException e) {
            log.error("Deserialize exception", e);
            return null;
        }
    }
}
