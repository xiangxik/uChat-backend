package com.uchat.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = PostgreSqlJpaConfiguration.class,
        properties = {
                "uchat.storage.provider=postgres",
                "spring.datasource.url=jdbc:h2:mem:uchat;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver"
        }
)
class PostgreSqlJpaConfigurationConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void opensJdbcConnection() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
        }
    }
}