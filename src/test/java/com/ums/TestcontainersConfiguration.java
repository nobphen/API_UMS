package com.ums;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * A new, empty PostgreSQL and Redis in Docker for API tests (same images as docker-compose.yml).
 * Flyway builds the tables and the sample data (dev profile: db/migration + db/testdata, so the admin user exists).
 * The containers are removed after the test run: the dev database is never touched.
 * Use with {@code @Import(TestcontainersConfiguration.class)}.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    /** @ServiceConnection: Spring uses this database instead of DB_URL / localhost:5432. */
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:16");
    }

    /** @ServiceConnection: Spring uses this Redis instead of localhost:6379 (OTP codes, sessions, permission cache). */
    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redis() {
        return new GenericContainer<>("redis:7").withExposedPorts(6379);
    }
}
