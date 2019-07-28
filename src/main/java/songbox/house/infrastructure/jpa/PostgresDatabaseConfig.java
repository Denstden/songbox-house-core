package songbox.house.infrastructure.jpa;

import com.zaxxer.hikari.HikariConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(basePackages = "songbox.house", entityManagerFactoryRef = "postgresEmf", transactionManagerRef = "postgresTm")
public class PostgresDatabaseConfig extends HikariConfig {

    @Bean(name = "postgresDs")
    @Primary
    @ConfigurationProperties(prefix = "spring.postgres.datasource")
    public DataSource postgresDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "postgresEmf")
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(postgresDataSource())
                .packages("songbox.house.domain.entity")
                .persistenceUnit("postgres")
                .properties(jpaProperties())
                .build();
    }

    private Map<String, Object> jpaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.physical_naming_strategy", ImprovedCamelCaseNamingStrategy.class.getName());
        props.put("database-platform", org.hibernate.dialect.PostgreSQL94Dialect.class);
//        props.put("hibernate.hbm2ddl.auto", "update");
        return props;
    }

    @Bean(name = "postgresTm")
    @Autowired
    public PlatformTransactionManager transactionManager(
            @Qualifier("postgresEmf") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager(entityManagerFactory);
        return txManager;
    }
}
