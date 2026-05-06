package com.vpm.taskserver.config;

import com.vpm.taskserver.config.properties.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    private final RabbitMQProperties rabbitMQProperties;

    public RabbitMQConfig(RabbitMQProperties rabbitMQProperties) {
        this.rabbitMQProperties = rabbitMQProperties;
    }

    /*
     * Dead letter queues and exchanges configuration
     */

    @Bean
    public Queue volunteerAssignedToTaskDQL() {
        return QueueBuilder
                .durable(rabbitMQProperties.getQueue().getDlq().getVolunteerAssigned())
                .build();
    }

    @Bean
    public Queue volunteerReportedTaskSuggestionDQL() {
        return QueueBuilder
                .durable(rabbitMQProperties.getQueue().getDlq().getVolunteerSuggestionReported())
                .build();
    }

    @Bean
    public DirectExchange volunteerAssignedToTaskDLX() {
        return new DirectExchange(rabbitMQProperties.getExchange().getDlx().getVolunteerAssigned());
    }

    @Bean
    public DirectExchange volunteerReportedTaskSuggestionDLX() {
        return new DirectExchange(rabbitMQProperties.getExchange().getDlx().getVolunteerSuggestionReported());
    }

    @Bean
    public Binding volunteerAssignedToTaskDLQBinding() {
        return BindingBuilder
                .bind(volunteerAssignedToTaskDQL())
                .to(volunteerAssignedToTaskDLX())
                .with(rabbitMQProperties.getRoutingKey().getDlq().getVolunteerAssigned());
    }

    @Bean
    public Binding volunteerReportedTaskSuggestionDLQBinding() {
        return BindingBuilder
                .bind(volunteerReportedTaskSuggestionDQL())
                .to(volunteerReportedTaskSuggestionDLX())
                .with(rabbitMQProperties.getRoutingKey().getDlq().getVolunteerSuggestionReported());
    }

    /*
     * Queues with DLQ configuration
     */

    @Bean
    public Queue volunteerAssignedToTaskQueue() {
        return QueueBuilder
                .durable(rabbitMQProperties.getQueue().getVolunteerAssigned())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getExchange().getDlx().getVolunteerAssigned())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getRoutingKey().getDlq().getVolunteerAssigned())
                .withArgument("x-message-ttl", 86400000) // Message TTL of 24h
                .build();
    }

    @Bean
    public Queue volunteerReportedTaskSuggestionQueue() {
        return QueueBuilder
                .durable(rabbitMQProperties.getQueue().getVolunteerSuggestionReported())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getExchange().getDlx().getVolunteerSuggestionReported())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getRoutingKey().getDlq().getVolunteerSuggestionReported())
                .withArgument("x-message-ttl", 86400000) // Message TTL of 24h
                .build();
    }

    /*
     * Exchanges configuration
     */

    @Bean
    public DirectExchange volunteerAssignedToTaskExchange() {
        return new DirectExchange(rabbitMQProperties.getExchange().getVolunteerAssigned());
    }

    @Bean
    public DirectExchange volunteerReportedTaskSuggestionExchange() {
        return new DirectExchange(rabbitMQProperties.getExchange().getVolunteerSuggestionReported());
    }

    /*
     * Binding configuration
     */

    @Bean
    public Binding volunteerAssignedToTaskExchangeBinding(
            Queue volunteerAssignedToTaskQueue,
            DirectExchange volunteerAssignedToTaskExchange
    ) {
        return BindingBuilder
                .bind(volunteerAssignedToTaskQueue)
                .to(volunteerAssignedToTaskExchange)
                .with(rabbitMQProperties.getRoutingKey().getVolunteerAssigned());
    }

    @Bean
    public Binding volunteerReportedTaskSuggestionExchangeBinding(
            Queue volunteerReportedTaskSuggestionQueue,
            DirectExchange volunteerReportedTaskSuggestionExchange
    ) {
        return BindingBuilder
                .bind(volunteerReportedTaskSuggestionQueue)
                .to(volunteerReportedTaskSuggestionExchange)
                .with(rabbitMQProperties.getRoutingKey().getVolunteerSuggestionReported());
    }

    /*
     * RabbitTemplate with confirms
     */

    @Bean
    public RabbitTemplate getRabbitTemplate(
            ConnectionFactory connectionFactory
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
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

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
