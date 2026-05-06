package com.vpm.volunteerserver.integration.service;

import com.vpm.volunteerserver.config.IntegrationTestsDBConfig;
import com.vpm.volunteerserver.config.IntegrationTestsRabbitMQConfig;
import com.vpm.volunteerserver.dto.event.VolunteerAssignedToProjectEvent;
import com.vpm.volunteerserver.dto.event.VolunteerAssignedToTaskEvent;
import com.vpm.volunteerserver.dto.event.VolunteerReportedTaskSuggestionEvent;
import com.vpm.volunteerserver.exception.volunteer.NoSuchVolunteerException;
import com.vpm.volunteerserver.service.event.VolunteerEventService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({
        IntegrationTestsDBConfig.class,
        IntegrationTestsRabbitMQConfig.class
})
@DisplayName("VolunteerEventHandler Integration Tests")
@Slf4j
public class VolunteerEventHandlerTest {

    @MockitoBean
    VolunteerEventService volunteerEventService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Exchange configurations
    private static final String EXCHANGE_VOLUNTEER_PROJECT = "exchange.volunteer-project";
    private static final String EXCHANGE_VOLUNTEER_TASK = "exchange.volunteer-task";
    private static final String EXCHANGE_VOLUNTEER_SUGGESTION = "exchange.volunteer-suggestion";

    // Routing keys
    private static final String ROUTING_KEY_PROJECT = "volunteer-project.assigned";
    private static final String ROUTING_KEY_TASK = "volunteer-task.assigned";
    private static final String ROUTING_KEY_SUGGESTION = "volunteer-suggestion.reported";

    // Dead Letter Queues and exchanges
    private static final String DLQ_VOLUNTEER_PROJECT = "dlq.volunteer-project";
    private static final String DLQ_VOLUNTEER_TASK = "dlq.volunteer-task";
    private static final String DLQ_VOLUNTEER_SUGGESTION = "dlq.volunteer-suggestion";

    private static final String DLX_VOLUNTEER_PROJECT = "dlx.volunteer-project";
    private static final String DLX_VOLUNTEER_TASK = "dlx.volunteer-task";
    private static final String DLX_VOLUNTEER_SUGGESTION = "dlx.volunteer-suggestion";

    // Test data constants
    private static final Long VOLUNTEER_ID = 1L;
    private static final Long PROJECT_ID = 1L;
    private static final Long TASK_ID = 2L;

    private VolunteerAssignedToProjectEvent assignedToProjectEvent;
    private VolunteerAssignedToTaskEvent assignedToTaskEvent;
    private VolunteerReportedTaskSuggestionEvent suggestedTaskEvent;

    /**
     * Sends a message to RabbitMQ with proper event headers and correlation ID
     */
    private void sendEventToRabbitMQ(Object event, String exchange, String routingKey, String eventType) {
        String correlationId = java.util.UUID.randomUUID().toString();

        MessagePostProcessor messagePostProcessor = message -> {
            message.getMessageProperties().setCorrelationId(correlationId);
            message.getMessageProperties().setHeader("eventType", eventType);
            return message;
        };

        CorrelationData correlationData = new CorrelationData(correlationId);

        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                event,
                messagePostProcessor,
                correlationData
        );
    }

    /**
     * Sets up test event for VolunteerAssignedToProject
     */
    private void setupVolunteerAssignedToProjectEvent() {
        assignedToProjectEvent = new VolunteerAssignedToProjectEvent();
        assignedToProjectEvent.setProjectId(PROJECT_ID);
        assignedToProjectEvent.setVolunteerId(VOLUNTEER_ID);
        assignedToProjectEvent.setAssignedDate(java.time.LocalDate.now());
    }

    /**
     * Sets up test event for VolunteerAssignedToTask
     */
    private void setupVolunteerAssignedToTaskEvent() {
        assignedToTaskEvent = new VolunteerAssignedToTaskEvent();
        assignedToTaskEvent.setTaskId(TASK_ID);
        assignedToTaskEvent.setVolunteerId(VOLUNTEER_ID);
        assignedToTaskEvent.setAssignedDate(java.time.LocalDate.now());
    }

    /**
     * Sets up test event for VolunteerReportedTaskSuggestion
     */
    private void setupVolunteerReportedTaskSuggestionEvent() {
        suggestedTaskEvent = new VolunteerReportedTaskSuggestionEvent();
        suggestedTaskEvent.setTaskId(TASK_ID);
        suggestedTaskEvent.setVolunteerId(VOLUNTEER_ID);
        suggestedTaskEvent.setAssignedDate(java.time.LocalDate.now());
    }

    @Nested
    @DisplayName("Successful Event Processing Tests")
    class SuccessfulEventProcessingTests {

        @Test
        @DisplayName("Test handling of VolunteerAssignedToProjectEvent")
        void testHandleVolunteerAssignedToProjectEvent() {
            setupVolunteerAssignedToProjectEvent();

            sendEventToRabbitMQ(
                    assignedToProjectEvent,
                    EXCHANGE_VOLUNTEER_PROJECT,
                    ROUTING_KEY_PROJECT,
                    "VolunteerAssignedToProjectEvent"
            );

            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService)
                                    .assignVolunteerToProject(assignedToProjectEvent)
                    );
        }

        @Test
        @DisplayName("Test handling of VolunteerAssignedToTaskEvent")
        void testHandleVolunteerAssignedToTaskEvent() {
            setupVolunteerAssignedToTaskEvent();

            sendEventToRabbitMQ(
                    assignedToTaskEvent,
                    EXCHANGE_VOLUNTEER_TASK,
                    ROUTING_KEY_TASK,
                    "VolunteerAssignedToTaskEvent"
            );

            Awaitility.await().atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService)
                                    .assignVolunteerToTask(assignedToTaskEvent)
                    );
        }

        @Test
        @DisplayName("Test handling of VolunteerReportedTaskSuggestionEvent")
        void testHandleVolunteerReportedTaskSuggestionEvent() {
            setupVolunteerReportedTaskSuggestionEvent();

            sendEventToRabbitMQ(
                    suggestedTaskEvent,
                    EXCHANGE_VOLUNTEER_SUGGESTION,
                    ROUTING_KEY_SUGGESTION,
                    "VolunteerReportedTaskSuggestionEvent"
            );

            Awaitility.await().atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService)
                                    .assignVolunteerToSuggestedTask(suggestedTaskEvent)
                    );
        }
    }

    @Nested
    @DisplayName("Non-Retryable Exception Tests")
    class NonRetryableExceptionTests {

        @BeforeEach
        void setUp() {
            reset(volunteerEventService);
        }

        @Test
        @DisplayName("Should handle NoSuchVolunteerException (non-retryable)")
        void shouldHandleNoSuchVolunteerException() throws Exception {
            setupVolunteerAssignedToProjectEvent();

            doThrow(new NoSuchVolunteerException(VOLUNTEER_ID))
                    .when(volunteerEventService)
                    .assignVolunteerToProject(any(VolunteerAssignedToProjectEvent.class));

            sendEventToRabbitMQ(
                    assignedToProjectEvent,
                    EXCHANGE_VOLUNTEER_PROJECT,
                    ROUTING_KEY_PROJECT,
                    "VolunteerAssignedToProjectEvent"
            );

            // Verify service was called
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService, times(1))
                                    .assignVolunteerToProject(any(VolunteerAssignedToProjectEvent.class))
                    );

            // NoSuchVolunteerException should NOT be retried - sent directly to DLQ
            // Verify it was called only once (no retries)
            verify(volunteerEventService, times(1))
                    .assignVolunteerToProject(any(VolunteerAssignedToProjectEvent.class));
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException (non-retryable)")
        void shouldHandleIllegalArgumentException() throws Exception {
            setupVolunteerAssignedToTaskEvent();

            doThrow(new IllegalArgumentException("Invalid event data"))
                    .when(volunteerEventService)
                    .assignVolunteerToTask(any(VolunteerAssignedToTaskEvent.class));

            sendEventToRabbitMQ(
                    assignedToTaskEvent,
                    EXCHANGE_VOLUNTEER_TASK,
                    ROUTING_KEY_TASK,
                    "VolunteerAssignedToTaskEvent"
            );

            // Verify service was called once - IllegalArgumentException should NOT be retried
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService, times(1))
                                    .assignVolunteerToTask(any(VolunteerAssignedToTaskEvent.class))
                    );
        }

        @Test
        @DisplayName("Should handle IllegalStateException (non-retryable)")
        void shouldHandleIllegalStateException() throws Exception {
            setupVolunteerReportedTaskSuggestionEvent();

            doThrow(new IllegalStateException("Invalid state"))
                    .when(volunteerEventService)
                    .assignVolunteerToSuggestedTask(any(VolunteerReportedTaskSuggestionEvent.class));

            sendEventToRabbitMQ(
                    suggestedTaskEvent,
                    EXCHANGE_VOLUNTEER_SUGGESTION,
                    ROUTING_KEY_SUGGESTION,
                    "VolunteerReportedTaskSuggestionEvent"
            );

            // Verify service was called once - IllegalStateException should NOT be retried
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService, times(1))
                                    .assignVolunteerToSuggestedTask(any(VolunteerReportedTaskSuggestionEvent.class))
                    );
        }

        @Test
        @DisplayName("Should not retry NoSuchVolunteerException")
        void shouldNotRetryNoSuchVolunteerException() throws Exception {
            setupVolunteerAssignedToProjectEvent();

            doThrow(new NoSuchVolunteerException(VOLUNTEER_ID))
                    .when(volunteerEventService)
                    .assignVolunteerToSuggestedTask(any(VolunteerReportedTaskSuggestionEvent.class));

            sendEventToRabbitMQ(
                    assignedToProjectEvent,
                    EXCHANGE_VOLUNTEER_PROJECT,
                    ROUTING_KEY_PROJECT,
                    "VolunteerAssignedToProjectEvent"
            );

            // Wait a bit and verify it was NOT retried
            Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService, times(1))
                                    .assignVolunteerToProject(any(VolunteerAssignedToProjectEvent.class))
                    );
        }
    }

    @Nested
    @DisplayName("Retryable Exception Tests")
    class RetryableExceptionTests {

        @BeforeEach
        void setUp() {
            reset(volunteerEventService);
        }

        @Test
        @DisplayName("Should retry on RuntimeException (retryable)")
        void shouldRetryOnRuntimeException() throws Exception {
            setupVolunteerAssignedToProjectEvent();

            // Mock to throw a retryable exception (RuntimeException)
            doThrow(new RuntimeException("Temporary error"))
                    .when(volunteerEventService)
                    .assignVolunteerToProject(any(VolunteerAssignedToProjectEvent.class));

            sendEventToRabbitMQ(
                    assignedToProjectEvent,
                    EXCHANGE_VOLUNTEER_PROJECT,
                    ROUTING_KEY_PROJECT,
                    "VolunteerAssignedToProjectEvent"
            );

            // Wait longer to allow retries
            Awaitility.await().atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService, atLeastOnce())
                                    .assignVolunteerToProject(any(VolunteerAssignedToProjectEvent.class))
                    );

            // Should be called multiple times (at least 2 - initial + retry)
            Awaitility.await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService, atLeast(2))
                                    .assignVolunteerToProject(any(VolunteerAssignedToProjectEvent.class))
                    );
        }

        @Test
        @DisplayName("Should retry on NullPointerException (retryable)")
        void shouldRetryOnNullPointerException() throws Exception {
            setupVolunteerAssignedToTaskEvent();

            doThrow(new NullPointerException("Null value encountered"))
                    .when(volunteerEventService)
                    .assignVolunteerToTask(any(VolunteerAssignedToTaskEvent.class));

            sendEventToRabbitMQ(
                    assignedToTaskEvent,
                    EXCHANGE_VOLUNTEER_TASK,
                    ROUTING_KEY_TASK,
                    "VolunteerAssignedToTaskEvent"
            );

            // Should be retried multiple times
            Awaitility.await().atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService, atLeast(2))
                                    .assignVolunteerToTask(any(VolunteerAssignedToTaskEvent.class))
                    );
        }

        @Test
        @DisplayName("Should respect max retry attempts for retryable exceptions")
        void shouldRespectMaxRetryAttempts() throws Exception {
            setupVolunteerReportedTaskSuggestionEvent();

            doThrow(new RuntimeException("Persistent temporary error"))
                    .when(volunteerEventService)
                    .assignVolunteerToSuggestedTask(any(VolunteerReportedTaskSuggestionEvent.class));

            sendEventToRabbitMQ(
                    suggestedTaskEvent,
                    EXCHANGE_VOLUNTEER_SUGGESTION,
                    ROUTING_KEY_SUGGESTION,
                    "VolunteerReportedTaskSuggestionEvent"
            );

            // Wait for retries to complete (3 max retries configured)
            Awaitility.await().atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        // Should be called multiple times but not infinitely
                        // With max-retries: 3, should be max 4 calls total (1 initial + 3 retries)
                        verify(volunteerEventService, times(4))
                                .assignVolunteerToSuggestedTask(any(VolunteerReportedTaskSuggestionEvent.class));
                    });
        }
    }

    @Nested
    @DisplayName("Multiple Event Processing Tests")
    class MultipleEventProcessingTests {

        @Test
        @DisplayName("Should handle multiple events concurrently")
        void shouldHandleMultipleEventsConcurrently() {
            setupVolunteerAssignedToProjectEvent();
            setupVolunteerAssignedToTaskEvent();
            setupVolunteerReportedTaskSuggestionEvent();
            sendEventToRabbitMQ(
                    assignedToProjectEvent,
                    EXCHANGE_VOLUNTEER_PROJECT,
                    ROUTING_KEY_PROJECT,
                    "VolunteerAssignedToProjectEvent"
            );
            sendEventToRabbitMQ(
                    assignedToTaskEvent,
                    EXCHANGE_VOLUNTEER_TASK,
                    ROUTING_KEY_TASK,
                    "VolunteerAssignedToTaskEvent"
            );
            sendEventToRabbitMQ(
                    suggestedTaskEvent,
                    EXCHANGE_VOLUNTEER_SUGGESTION,
                    ROUTING_KEY_SUGGESTION,
                    "VolunteerReportedTaskSuggestionEvent"
            );

            Awaitility.await().atMost(20, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        verify(volunteerEventService)
                                .assignVolunteerToProject(any(VolunteerAssignedToProjectEvent.class));
                        verify(volunteerEventService)
                                .assignVolunteerToTask(any(VolunteerAssignedToTaskEvent.class));
                        verify(volunteerEventService)
                                .assignVolunteerToSuggestedTask(any(VolunteerReportedTaskSuggestionEvent.class));
                    });
        }

        @Test
        @DisplayName("Should process multiple events of same type")
        void shouldProcessMultipleEventsOfSameType() {
            // Send 3 project assignment events
            for (int i = 0; i < 3; i++) {
                VolunteerAssignedToProjectEvent event = new VolunteerAssignedToProjectEvent();
                event.setVolunteerId(VOLUNTEER_ID + i);
                event.setProjectId(PROJECT_ID + i);
                event.setAssignedDate(java.time.LocalDate.now());

                sendEventToRabbitMQ(
                        event,
                        EXCHANGE_VOLUNTEER_PROJECT,
                        ROUTING_KEY_PROJECT,
                        "VolunteerAssignedToProjectEvent"
                );
            }

            Awaitility.await().atMost(20, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            verify(volunteerEventService, times(3))
                                    .assignVolunteerToProject(any(VolunteerAssignedToProjectEvent.class))
                    );
        }
    }

}
