package songbox.house.infrastructure;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public StrongPasswordEncryptor strongPasswordEncryptor() {
        return new StrongPasswordEncryptor();
    }
}
