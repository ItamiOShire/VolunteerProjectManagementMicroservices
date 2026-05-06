package com.vpm.volunteerserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.volunteerserver.config.properties.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
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

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true); // important
        return admin;
    }

    /*
     * Dead letter queues and exchanges configuration
     */

    @Bean
    public Queue volunteerAssignedToTaskDQL() {
        return QueueBuilder
                .durable(rabbitMQProperties.getQueue().getDlq().getVolunteerAssignedToTask())
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
        return new DirectExchange(rabbitMQProperties.getExchange().getDlx().getVolunteerAssignedToTask());
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
                .with(rabbitMQProperties.getRoutingKey().getDlq().getVolunteerAssignedToTask());
    }

    @Bean
    public Binding volunteerReportedTaskSuggestionDLQBinding() {
        return BindingBuilder
                .bind(volunteerReportedTaskSuggestionDQL())
                .to(volunteerReportedTaskSuggestionDLX())
                .with(rabbitMQProperties.getRoutingKey().getDlq().getVolunteerSuggestionReported());
    }

    @Bean
    public Queue volunteerAssignedToProjectDQL() {
        return QueueBuilder
                .durable(rabbitMQProperties.getQueue().getDlq().getVolunteerAssignedToProject())
                .build();
    }

    @Bean
    public DirectExchange volunteerAssignedToProjectDLX() {
        return new DirectExchange(rabbitMQProperties.getExchange().getDlx().getVolunteerAssignedToProject());
    }

    @Bean
    public Binding volunteerAssignedToProjectDLQBinding() {
        return BindingBuilder
                .bind(volunteerAssignedToProjectDQL())
                .to(volunteerAssignedToProjectDLX())
                .with(rabbitMQProperties.getRoutingKey().getDlq().getVolunteerAssignedToProject());
    }

    /*
     * Queues with DLQ configuration
     */

    @Bean
    public Queue volunteerAssignedToTaskQueue() {
        return QueueBuilder
                .durable(rabbitMQProperties.getQueue().getVolunteerAssignedToTask())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getExchange().getDlx().getVolunteerAssignedToTask())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getRoutingKey().getDlq().getVolunteerAssignedToTask())
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

    @Bean
    public Queue volunteerAssignedToProjectQueue() {
        return QueueBuilder
                .durable(rabbitMQProperties.getQueue().getVolunteerAssignedToProject())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getExchange().getDlx().getVolunteerAssignedToProject())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getRoutingKey().getDlq().getVolunteerAssignedToProject())
                .withArgument("x-message-ttl", 60000) // Message TTL of 60 seconds
                .build();
    }

    /*
     * Exchanges configuration
     */

    @Bean
    public DirectExchange volunteerAssignedToTaskExchange() {
        return new DirectExchange(rabbitMQProperties.getExchange().getVolunteerAssignedToTask());
    }

    @Bean
    public DirectExchange volunteerReportedTaskSuggestionExchange() {
        return new DirectExchange(rabbitMQProperties.getExchange().getVolunteerSuggestionReported());
    }

    @Bean
    public DirectExchange volunteerAssignedToProjectExchange() {
        return new DirectExchange(rabbitMQProperties.getExchange().getVolunteerAssignedToProject());
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
                .with(rabbitMQProperties.getRoutingKey().getVolunteerAssignedToTask());
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

    @Bean
    public Binding volunteerAssignedToProjectExchangeBinding(
            Queue volunteerAssignedToProjectQueue,
            DirectExchange volunteerAssignedToProjectExchange
    ) {
        return BindingBuilder
                .bind(volunteerAssignedToProjectQueue)
                .to(volunteerAssignedToProjectExchange)
                .with(rabbitMQProperties.getRoutingKey().getVolunteerAssignedToProject());
    }

    /*
     * RabbitTemplate for publishing with confirmation callbacks
     *
     * Listener-side retry is configured via application.yaml:
     * spring.rabbitmq.listener.simple.retry.*
     *
     * This template is only for publishing messages.
     */
    @Bean
    public RabbitTemplate getRabbitTemplate(
            ConnectionFactory connectionFactory
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);

        // Confirm callback: Message reached broker?
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("Message confirmed by broker. CorrelationId: {}",
                        correlationData != null ? correlationData.getId() : "unknown");
            } else {
                log.error("Message NOT confirmed by broker. Cause: {}", cause);
            }
        });

        // Return callback: Message routed successfully?
        template.setReturnsCallback(returned ->
            log.error("Message returned from broker. Exchange: {}, RoutingKey: {}, ReplyCode: {}, ReplyText: {}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyCode(),
                    returned.getReplyText())
        );

        return template;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
