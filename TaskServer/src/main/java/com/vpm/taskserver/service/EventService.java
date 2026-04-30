package com.vpm.taskserver.service;

import com.vpm.taskserver.dto.event.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
     * @param exchange the RabbitMQ exchange name
     * @param eventType the type of event (for header)
     * @param <T> the type of event
     */
    public <T> void sendEvent(T event, String exchange, EventType eventType) {

        MessagePostProcessor messagePostProcessor = message -> {
            message.getMessageProperties().setCorrelationId(UUID.randomUUID().toString());
            message.getMessageProperties().setHeader("eventType", eventType);
            return message;
        };

        log.info("Attempting to send event of type: {}", eventType);

        rabbitTemplate.convertAndSend(
                exchange,
                event,
                messagePostProcessor
        );

        log.info("Event of type {} successfully sent", eventType);

    }

}
