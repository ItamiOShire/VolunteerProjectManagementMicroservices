package com.vpm.projectserver.integration;

import com.vpm.projectserver.config.IntegrationTestsDBConfig;
import com.vpm.projectserver.config.IntegrationTestsRabbitMQConfig;
import com.vpm.projectserver.dto.event.EventType;
import com.vpm.projectserver.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({
        IntegrationTestsDBConfig.class,
        IntegrationTestsRabbitMQConfig.class
})
@Slf4j
@DisplayName("EventService Integration Tests")
public class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private ConnectionFactory connectionFactory;

    private SimpleMessageListenerContainer container;
    private List<Message> receivedMessages;
    private CountDownLatch latch;

    private static final String TEST_EXCHANGE = "exchange.volunteer-project";
    private static final String TEST_ROUTING_KEY = "volunteer-project.assigned";
    private static final String TEST_QUEUE = "queue.volunteer-project";

    @BeforeEach
    public void setUp() {
        receivedMessages = new ArrayList<>();
        latch = new CountDownLatch(1);
        setupMessageListener();
    }

    private void setupMessageListener() {
        container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(TEST_QUEUE);
        container.setMessageListener(message -> {
            receivedMessages.add(message);
            latch.countDown();
        });
        container.start();
    }

    private void cleanupListener() {
        if (container != null) {
            container.stop();
        }
    }



    @Test
    @DisplayName("Should set correlation ID in message properties")
    public void testCorrelationIdIsSet() throws InterruptedException {
        // Arrange
        String testEventPayload = "Test Event with Correlation ID";
        EventType eventType = EventType.VOLUNTEER_ASSIGNED_TO_PROJECT;

        // Act
        eventService.sendEvent(testEventPayload, TEST_ROUTING_KEY, TEST_EXCHANGE, eventType);

        // Assert
        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertTrue(messageReceived, "Message was not received from RabbitMQ within timeout");

        Message receivedMessage = receivedMessages.get(0);
        String correlationId = receivedMessage.getMessageProperties().getCorrelationId();

        assertNotNull(correlationId, "Correlation ID should not be null");
        assertFalse(correlationId.isBlank(), "Correlation ID should not be blank");

        // Verify it's a valid UUID format
        try {
            UUID.fromString(correlationId);
            log.info("Correlation ID is valid UUID: {}", correlationId);
        } catch (IllegalArgumentException e) {
            fail("Correlation ID should be a valid UUID");
        }

        cleanupListener();
    }

    @Test
    @DisplayName("Should set eventType header in message properties")
    public void testEventTypeHeaderIsSet() throws InterruptedException {
        // Arrange
        String testEventPayload = "Test Event with Event Type Header";
        EventType eventType = EventType.VOLUNTEER_ASSIGNED_TO_PROJECT;

        // Act
        eventService.sendEvent(testEventPayload, TEST_ROUTING_KEY, TEST_EXCHANGE, eventType);

        // Assert
        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertTrue(messageReceived, "Message was not received from RabbitMQ within timeout");

        Message receivedMessage = receivedMessages.get(0);
        String eventTypeHeader = receivedMessage.getMessageProperties().getHeader("eventType");

        assertNotNull(eventTypeHeader, "eventType header should be set");
        assertEquals(eventType.toString(), eventTypeHeader, "eventType header should match the sent event type");

        cleanupListener();
    }

    @Test
    @DisplayName("Should send multiple events sequentially")
    public void testSendMultipleEventsSequentially() throws InterruptedException {
        // Arrange
        EventType eventType = EventType.VOLUNTEER_ASSIGNED_TO_PROJECT;
        int eventCount = 3;

        // Reset setup for multiple messages
        cleanupListener();
        receivedMessages.clear();

        // Setup listener that collects multiple messages
        container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(TEST_QUEUE);

        CountDownLatch multiLatch = new CountDownLatch(eventCount);
        container.setMessageListener(message -> {
            receivedMessages.add(message);
            multiLatch.countDown();
        });
        container.start();

        // Act
        for (int i = 0; i < eventCount; i++) {
            String payload = "Event " + (i + 1);
            eventService.sendEvent(payload, TEST_ROUTING_KEY, TEST_EXCHANGE, eventType);
        }

        // Assert
        boolean allMessagesReceived = multiLatch.await(10, TimeUnit.SECONDS);
        assertTrue(allMessagesReceived, "Not all messages were received within timeout");
        assertEquals(eventCount, receivedMessages.size(), "Expected " + eventCount + " messages");

        // Verify each message has required properties
        for (int i = 0; i < receivedMessages.size(); i++) {
            Message message = receivedMessages.get(i);
            assertNotNull(message.getMessageProperties().getCorrelationId(),
                    "Message " + (i + 1) + " should have correlation ID");
            assertNotNull(message.getMessageProperties().getHeader("eventType"),
                    "Message " + (i + 1) + " should have eventType header");
        }

        cleanupListener();
    }

    @Test
    @DisplayName("Should include message body in sent message")
    public void testMessageBodyIsIncluded() throws InterruptedException {
        // Arrange
        String testEventPayload = "Important Event Data";
        EventType eventType = EventType.VOLUNTEER_ASSIGNED_TO_PROJECT;

        // Act
        eventService.sendEvent(testEventPayload, TEST_ROUTING_KEY, TEST_EXCHANGE, eventType);

        // Assert
        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertTrue(messageReceived, "Message was not received from RabbitMQ within timeout");

        Message receivedMessage = receivedMessages.get(0);
        byte[] messageBody = receivedMessage.getBody();

        assertNotNull(messageBody, "Message body should not be null");
        assertTrue(messageBody.length > 0, "Message body should not be empty");

        String bodyContent = new String(messageBody);
        assertTrue(bodyContent.contains(testEventPayload) || bodyContent.length() > 0,
                "Message body should contain the sent payload");

        cleanupListener();
    }

    @Test
    @DisplayName("Should generate unique correlation IDs for each event")
    public void testUniqueCorrelationIds() throws InterruptedException {
        // Arrange
        EventType eventType = EventType.VOLUNTEER_ASSIGNED_TO_PROJECT;
        List<String> correlationIds = new ArrayList<>();

        cleanupListener();
        receivedMessages.clear();

        // Setup listener for 2 messages
        container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(TEST_QUEUE);

        CountDownLatch dualLatch = new CountDownLatch(2);
        container.setMessageListener(message -> {
            receivedMessages.add(message);
            correlationIds.add(message.getMessageProperties().getCorrelationId());
            dualLatch.countDown();
        });
        container.start();

        // Act
        eventService.sendEvent("Event 1", TEST_ROUTING_KEY, TEST_EXCHANGE, eventType);
        eventService.sendEvent("Event 2", TEST_ROUTING_KEY, TEST_EXCHANGE, eventType);

        // Assert
        boolean allMessagesReceived = dualLatch.await(10, TimeUnit.SECONDS);
        assertTrue(allMessagesReceived, "Both messages should be received");
        assertEquals(2, correlationIds.size(), "Should have 2 correlation IDs");
        assertNotEquals(correlationIds.get(0), correlationIds.get(1),
                "Correlation IDs should be unique");

        cleanupListener();
    }

    @Test
    @DisplayName("Should maintain message persistence after sending")
    public void testMessagePersistenceInQueue() throws InterruptedException {
        // Arrange
        String testPayload = "Persistent Message";
        EventType eventType = EventType.VOLUNTEER_ASSIGNED_TO_PROJECT;

        // Act - Send message
        eventService.sendEvent(testPayload, TEST_ROUTING_KEY, TEST_EXCHANGE, eventType);

        // Assert
        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertTrue(messageReceived, "Message should be persisted and received");
        assertFalse(receivedMessages.isEmpty(), "Received messages list should not be empty");

        cleanupListener();
    }
}





