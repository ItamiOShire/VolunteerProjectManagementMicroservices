package com.vpm.projectserver.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class IntegrationTestsDBConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {

        return new PostgreSQLContainer<>("postgres:17.9")
                .withDatabaseName("Projects")
                .withPassword("password")
                .withUsername("admin");

    }

}
