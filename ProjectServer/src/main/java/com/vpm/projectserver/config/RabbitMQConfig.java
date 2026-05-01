package com.vpm.projectserver.config;

import com.vpm.projectserver.config.properties.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    private final RabbitMQProperties rabbitMQProperties;

    @Autowired
    public RabbitMQConfig(RabbitMQProperties rabbitMQProperties) {
        this.rabbitMQProperties = rabbitMQProperties;
    }


    /*
     * Dead letter queues and exchanges configuration
     */

    @Bean
    public Queue volunteerAssignedToProjectDQL() {
        return QueueBuilder
                .durable(rabbitMQProperties.getQueue().getDlq().getVolunteerAssigned())
                .build();
    }

    @Bean
    public DirectExchange volunteerAssignedToProjectDLX() {
        return new DirectExchange(rabbitMQProperties.getExchange().getDlx().getVolunteerAssigned());
    }

    @Bean
    public Binding volunteerAssignedToProjectDLQBinding() {
        return BindingBuilder
                .bind(volunteerAssignedToProjectDQL())
                .to(volunteerAssignedToProjectDLX())
                .with(rabbitMQProperties.getRoutingKey().getDlq().getVolunteerAssigned());
    }

    /*
     * Queues with DLQ configuration
     */

    @Bean
    public Queue volunteerAssignedToProjectQueue() {
        return QueueBuilder
                .durable(rabbitMQProperties.getQueue().getVolunteerAssigned())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getExchange().getDlx().getVolunteerAssigned())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getRoutingKey().getDlq().getVolunteerAssigned())
                .withArgument("x-message-ttl", 60000) // Message TTL of 60 seconds
                .build();
    }

    /*
     * Exchanges configuration
     */

    @Bean
    public DirectExchange volunteerAssignedToProjectExchange() {
        return new DirectExchange(rabbitMQProperties.getExchange().getVolunteerAssigned());
    }

    /*
     * Binding configuration
     */

    @Bean
    public Binding volunteerAssignedToProjectExchangeBinding(
            Queue volunteerAssignedToProjectQueue,
            DirectExchange volunteerAssignedToProjectExchange
    ) {
        return BindingBuilder
                .bind(volunteerAssignedToProjectQueue)
                .to(volunteerAssignedToProjectExchange)
                .with(rabbitMQProperties.getRoutingKey().getVolunteerAssigned());
    }

    /*
     * RabbitTemplate with confirms
     */

    @Bean
    public RabbitTemplate getRabbitTemplate(
            ConnectionFactory connectionFactory
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(true);

        template.setConfirmCallback(
                (correlationData, ack, cause) -> {
                    if (!ack) {
                        log.error("Failed to publish message: {}", cause);
                    } else {
                        log.info("Message published successfully with id: {}",
                                correlationData != null ? correlationData.getId() : "unknown");
                    }
                }
        );

        template.setReturnsCallback(
                returned -> log.error("Failed to route message: {} from exchange: {} with routing key: {}",
                        returned.getMessage(),
                        returned.getExchange(),
                        returned.getRoutingKey())
        );

        return template;
    }

}
