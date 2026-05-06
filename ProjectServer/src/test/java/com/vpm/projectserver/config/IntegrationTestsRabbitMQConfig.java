package com.vpm.projectserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;


@TestConfiguration
@Slf4j
public class IntegrationTestsRabbitMQConfig {

    private static final String QUEUE_VOLUNTEER_ASSIGNED = "test.queue.volunteer-project";
    private static final String DLQ_VOLUNTEER_ASSIGNED = "test.dlq.volunteer-project";

    private static final String EXCHANGE_VOLUNTEER_ASSIGNED = "test.exchange.volunteer-project";
    private static final String DLX_VOLUNTEER_ASSIGNED = "test.dlx.volunteer-project";

    private static final String ROUTING_KEY_VOLUNTEER_ASSIGNED = "test.routingkey.volunteer-project";
    private static final String DLQ_ROUTING_KEY_ASSIGNED = "test.dlq.routiongkey.volunteer-project";

    @Bean
    @ServiceConnection
    public RabbitMQContainer rabbitMQContainer() {
        return new RabbitMQContainer("rabbitmq:3.12-management-alpine")
                .withAdminUser("admin")
                .withAdminPassword("password");
    }

}


