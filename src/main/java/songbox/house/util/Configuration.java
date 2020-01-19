package songbox.house.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@org.springframework.context.annotation.Configuration
@PropertySource("classpath:env.properties")
@ConfigurationProperties(prefix = "configuration")
public class Configuration {
    public static class Proxy {
        private @Getter
        @Setter
        String ip;
        private @Getter
        @Setter
        Integer port;
    }

    public static class Vk {
        private String userId;

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Long getUserId() {
            return Long.parseLong(userId);
        }
    }

    public static class Connection {
        private @Getter
        @Setter
        String vkCookie;
    }

    private @Getter
    @Setter
    Proxy proxy;
    private @Getter
    @Setter
    Vk vk;
    private @Getter
    @Setter
    Connection connection;

    public Map<String, String> parseCookies() {
        final String[] split = connection.vkCookie.split(";");
        return Stream.of(split)
                .map(str -> str.split("="))
                .map(keyValue -> Pair.of(keyValue[0].trim(), keyValue[1].trim()))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }
}
