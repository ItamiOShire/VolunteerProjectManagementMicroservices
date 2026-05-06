package com.vpm.taskserver.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@TestConfiguration
public class IntegrationTestsRabbitMQConfig {

    @Bean
    @ServiceConnection
    public RabbitMQContainer rabbitMQContainer() {
        return new RabbitMQContainer("rabbitmq:3.12-management-alpine")
                .withAdminUser("admin")
                .withAdminPassword("password");
    }

}
