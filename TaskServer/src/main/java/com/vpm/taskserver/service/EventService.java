package com.vpm.taskserver.service;

import com.vpm.taskserver.dto.event.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

// TODO: Consider making method of EventService asynchronous

@Service
@Slf4j
public class EventService {

    private final RabbitTemplate rabbitTemplate;

    public EventService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Generic method to send any event to RabbitMQ
     *
     * @param event the event object to send (already mapped from domain object)
     * @param routingKey the RabbitMQ routing key name
     * @param eventType the type of event (for header)
     * @param <T> the type of event
     */
    public <T> void sendEvent(T event, String routingKey, String exchange, EventType eventType) {

        String correlationId = UUID.randomUUID().toString();

        MessagePostProcessor messagePostProcessor = message -> {
            message.getMessageProperties().setCorrelationId(correlationId);
            message.getMessageProperties().setHeader("eventType", eventType);
            return message;
        };

        CorrelationData correlationData = new CorrelationData(correlationId);

        log.info("Attempting to send event of type: {}", eventType);

        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                event,
                messagePostProcessor,
                correlationData
        );

        log.info("Event of type {} successfully sent", eventType);
    }

}
