package com.vpm.volunteerserver.service.event;

import com.rabbitmq.client.Channel;
import com.vpm.volunteerserver.dto.event.VolunteerAssignedToProjectEvent;
import com.vpm.volunteerserver.dto.event.VolunteerAssignedToTaskEvent;
import com.vpm.volunteerserver.dto.event.VolunteerReportedTaskSuggestionEvent;
import com.vpm.volunteerserver.exception.volunteer.NoSuchVolunteerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;

/**
 * Event handler for RabbitMQ events from ProjectServer
 *
 * Handles 3 event types:
 * 1. VolunteerAssignedToTask
 * 2. VolunteerAssignedToProject
 * 3. VolunteerReportedTaskSuggestion
 *
 * Retry mechanism:
 * - Retryable errors: Re-queued with exponential backoff (1s → 2s → 4s)
 * - Non-retryable errors: Sent to Dead Letter Queue
 * - Max attempts: 3 (configured in application.yaml)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectEventHandler {

    private final VolunteerEventService volunteerEventService;

    /**
     * Exceptions that should NOT be retried - go directly to DLQ
     */
    private static final Set<Class<? extends Exception>> NON_RETRYABLE_EXCEPTIONS = Set.of(
        IllegalArgumentException.class,
        IllegalStateException.class,
        NoSuchVolunteerException.class
    );

    /**
     * Handles VolunteerAssignedToTask event
     *
     * Flow:
     * - Success: ACK (message removed)
     * - Retryable error: Spring RetryTemplate handles retry
     * - Non-retryable error: NACK (goes to DLQ immediately)
     */
    @RabbitListener(queues = "#{@rabbitMQProperties.getQueue().getVolunteerAssignedToTask()}")
    public void handleVolunteerAssignedToTaskEvent(
        @Payload VolunteerAssignedToTaskEvent event,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
        @Header(name = "eventType", required = false) String eventType,
        Channel channel
    ) throws IOException {

        log.info("Received VolunteerAssignedToTask event for volunteerId: {}, taskId: {}",
            event.getVolunteerId(), event.getTaskId());

        try {
            volunteerEventService.assignVolunteerToTask(event);

            channel.basicAck(deliveryTag, false);
            log.info("Successfully processed VolunteerAssignedToTask event");

        } catch (Exception e) {
            handleEventException(e, event, deliveryTag, channel, "VolunteerAssignedToTask");
        }
    }

    /**
     * Handles VolunteerAssignedToProject event
     */
    @RabbitListener(queues = "#{@rabbitMQProperties.getQueue().getVolunteerAssignedToProject()}")
    public void handleVolunteerAssignedToProjectEvent(
        @Payload VolunteerAssignedToProjectEvent event,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
        @Header(name = "eventType", required = false) String eventType,
        Channel channel
    ) throws IOException {

        log.info("Received VolunteerAssignedToProject event for volunteerId: {}, projectId: {}",
            event.getVolunteerId(), event.getProjectId());

        try {
            volunteerEventService.assignVolunteerToProject(event);

            channel.basicAck(deliveryTag, false);
            log.info("Successfully processed VolunteerAssignedToProject event");

        } catch (Exception e) {
            handleEventException(e, event, deliveryTag, channel, "VolunteerAssignedToProject");
        }
    }

    /**
     * Handles VolunteerReportedTaskSuggestion event
     */
    @RabbitListener(queues = "#{@rabbitMQProperties.getQueue().getVolunteerSuggestionReported()}")
    public void handleVolunteerReportedTaskSuggestionEvent(
        @Payload VolunteerReportedTaskSuggestionEvent event,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
        @Header(name = "eventType", required = false) String eventType,
        Channel channel
    ) throws IOException {

        log.info("Received VolunteerReportedTaskSuggestion event for volunteerId: {}, taskId: {}",
            event.getVolunteerId(), event.getTaskId());

        try {
            volunteerEventService.assignVolunteerToSuggestedTask(event);

            channel.basicAck(deliveryTag, false);
            log.info("Successfully processed VolunteerReportedTaskSuggestion event");

        } catch (Exception e) {
            handleEventException(e, event, deliveryTag, channel, "VolunteerReportedTaskSuggestion");
        }
    }

    /**
     * Common exception handler for all event types
     *
     * Decision logic:
     * - Non-retryable exceptions: Don't requeue (goes to DLQ)
     * - Retryable exceptions: Throw to trigger Spring's retry mechanism
     */
    private void handleEventException(
        Exception e,
        Object event,
        long deliveryTag,
        Channel channel,
        String eventType
    ) throws IOException {

        if (isNonRetryable(e)) {
            log.error("Non-retryable exception in {} event. Sending to DLQ. Exception: {}",
                eventType, e.getClass().getSimpleName(), e);

            // Send to DLQ - don't requeue
            channel.basicNack(deliveryTag, false, false);
        } else {
            log.warn("Retryable exception in {} event. Will retry. Exception: {}",
                eventType, e.getClass().getSimpleName(), e);

            // Let Spring's RetryTemplate handle the retry
            // Throw exception to trigger retry mechanism
            throw new RuntimeException("Event processing failed, will retry", e);
        }
    }

    /**
     * Check if exception (or its cause chain) is non-retryable
     * Handles wrapped exceptions properly
     */
    private boolean isNonRetryable(Exception e) {
        Throwable cause = e;

        while (cause != null) {
            // Check if this exception class should NOT be retried
            Throwable finalCause = cause;
            if (NON_RETRYABLE_EXCEPTIONS.stream()
                    .anyMatch(exceptionClass -> exceptionClass.isAssignableFrom(finalCause.getClass()))) {
                return true;  // Non-retryable
            }
            cause = cause.getCause();  // Check wrapped exceptions
        }
        return false;  // Retryable
    }

}
