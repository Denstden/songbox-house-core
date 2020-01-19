package songbox.house.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

@Component
@Slf4j
public class JsonUtils implements ApplicationContextAware {

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        mapper = applicationContext.getBean(ObjectMapper.class)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @SneakyThrows
    public static <T> T fromString(final String json, final Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.debug("Error parsing json {} to DTO {}", json, clazz);
        }

        return null;
    }
}
