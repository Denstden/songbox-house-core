package songbox.house.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

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
}
