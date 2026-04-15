package com.vpm.authenticationserver.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class IntegrationTestsDBConfig {

    /*
     * Database container managed by spring
     * image should be the same as production
     * database name, password and username does not matter
     */

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {

        return new PostgreSQLContainer<>("postgres:17.9")
                        .withDatabaseName("Users")
                        .withPassword("password")
                        .withUsername("admin");

    }

}
