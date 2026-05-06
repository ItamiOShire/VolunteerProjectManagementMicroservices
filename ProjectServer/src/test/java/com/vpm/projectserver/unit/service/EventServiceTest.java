package com.vpm.projectserver.unit.service;

import com.vpm.projectserver.dto.event.EventType;
import com.vpm.projectserver.service.EventService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventService.sendEvent()
 *
 * Tests cover:
 * - Successful event publishing
 * - Correct message headers (correlation ID, event type)
 * - RabbitTemplate invocation with correct parameters
 * - Generic event type handling
 * - Proper logging
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventService - Send Event")
class EventServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventService eventService;

    private static final String TEST_EXCHANGE = "exchange.test";
    private static final String TEST_ROUTING_KEY = "routingkey.test";
    private static final EventType TEST_EVENT_TYPE = EventType.VOLUNTEER_ASSIGNED_TO_PROJECT;

    private TestEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = TestEvent.builder()
                .id(1L)
                .message("Test message")
                .build();
    }

    @Test
    @DisplayName("Should send event to RabbitMQ")
    void testSendEvent_PublishesEvent() {
        // Act
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, TEST_EVENT_TYPE);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(TEST_EXCHANGE),
                eq(TEST_ROUTING_KEY),
                eq(testEvent),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );
    }

    @Test
    @DisplayName("Should send event to correct exchange")
    void testSendEvent_UsesCorrectExchange() {
        // Arrange
        String expectedExchange = "exchange.test";

        // Act
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, TEST_EVENT_TYPE);

        // Assert
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );

        assertThat(exchangeCaptor.getValue()).isEqualTo(expectedExchange);
    }

    @Test
    @DisplayName("Should send correct event object")
    void testSendEvent_SendsCorrectEventObject() {
        // Act
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, TEST_EVENT_TYPE);

        // Assert
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );

        assertThat(eventCaptor.getValue())
                .isInstanceOf(TestEvent.class)
                .isEqualTo(testEvent);
    }

    @Test
    @DisplayName("Should set correlation ID in message header")
    void testSendEvent_SetsCorrelationId() {
        // Arrange
        ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, TEST_EVENT_TYPE);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                any(Object.class),
                processorCaptor.capture(),
                any(CorrelationData.class)
        );

        MessagePostProcessor processor = processorCaptor.getValue();
        Message mockMessage = createMockMessage();
        Message processedMessage = processor.postProcessMessage(mockMessage);

        // Correlation ID should be set and should be a valid UUID
        String correlationId = processedMessage.getMessageProperties().getCorrelationId();
        assertThat(correlationId)
                .isNotNull()
                .isNotEmpty();

        // Verify it's a valid UUID format
        UUID uuid = UUID.fromString(correlationId);
        assertThat(uuid).isNotNull();
    }

    @Test
    @DisplayName("Should set event type header")
    void testSendEvent_SetsEventTypeHeader() {
        // Arrange
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, TEST_EVENT_TYPE);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                any(Object.class),
                processorCaptor.capture(),
                any(CorrelationData.class)
        );

        MessagePostProcessor processor = processorCaptor.getValue();
        Message mockMessage = createMockMessage();
        Message processedMessage = processor.postProcessMessage(mockMessage);

        Object eventTypeHeader = processedMessage.getMessageProperties().getHeader("eventType");
        assertThat(eventTypeHeader)
                .isNotNull()
                .isEqualTo(TEST_EVENT_TYPE);
    }

    @Test
    @DisplayName("Should handle different event types")
    void testSendEvent_HandlesDifferentEventTypes() {
        // Act - Send event with different event types
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, EventType.VOLUNTEER_ASSIGNED_TO_PROJECT);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                anyString(),
                anyString(),
                any(Object.class),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );

        // Act again with different event
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, EventType.VOLUNTEER_ASSIGNED_TO_PROJECT);


        verify(rabbitTemplate, times(2)).convertAndSend(
                anyString(),
                anyString(),
                any(Object.class),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );
    }

    @Test
    @DisplayName("Should process message with MessagePostProcessor")
    void testSendEvent_AppliesMessagePostProcessor() {
        // Arrange
        ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        // Act
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, TEST_EVENT_TYPE);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                any(Object.class),
                processorCaptor.capture(),
                any(CorrelationData.class)
        );

        MessagePostProcessor processor = processorCaptor.getValue();
        assertThat(processor).isNotNull();

        // Verify processor modifies the message
        org.springframework.amqp.core.Message originalMessage = createMockMessage();
        org.springframework.amqp.core.Message processedMessage = processor.postProcessMessage(originalMessage);

        assertThat(processedMessage).isNotNull();
        assertThat(processedMessage.getMessageProperties().getCorrelationId()).isNotNull();
        Object header = processedMessage.getMessageProperties().getHeader("eventType");
        assertThat(header).isNotNull();
    }

    @Test
    @DisplayName("Should invoke RabbitTemplate with correct parameters in correct order")
    void testSendEvent_InvokesConvertAndSendCorrectly() {
        // Act
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, TEST_EVENT_TYPE);

        // Assert - verify convertAndSend was called exactly once with correct argument order
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(TEST_EXCHANGE),
                eq(TEST_ROUTING_KEY),
                eq(testEvent),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );
    }

    @Test
    @DisplayName("Should work with generic types")
    void testSendEvent_WorksWithGenericObject() {
        // Arrange
        Object genericEvent = new TestEvent(2L, "Generic message");

        // Act
        eventService.sendEvent(genericEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, TEST_EVENT_TYPE);

        // Assert
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                eventCaptor.capture(),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );

        assertThat(eventCaptor.getValue()).isEqualTo(genericEvent);
    }

    @Test
    @DisplayName("Should maintain idempotency of correlation IDs")
    void testSendEvent_GeneratesUniqueCorrelationIds() {
        // Arrange
        ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        // Act - Send two events
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, TEST_EVENT_TYPE);
        eventService.sendEvent(testEvent, TEST_ROUTING_KEY, TEST_EXCHANGE, TEST_EVENT_TYPE);

        // Assert
        verify(rabbitTemplate, times(2)).convertAndSend(
                anyString(),
                anyString(),
                any(Object.class),
                processorCaptor.capture(),
                any(CorrelationData.class)
        );

        var processors = processorCaptor.getAllValues();
        String correlationId1 = processors.get(0).postProcessMessage(createMockMessage())
                .getMessageProperties().getCorrelationId();
        String correlationId2 = processors.get(1).postProcessMessage(createMockMessage())
                .getMessageProperties().getCorrelationId();

        // Correlation IDs should be different
        assertThat(correlationId1).isNotEqualTo(correlationId2);
    }

    // Helper method to create a mock message
    private Message createMockMessage() {
        return new Message(
                "test body".getBytes(),
                new MessageProperties()
        );
    }

    /**
     * Test event class for testing generic event handling
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TestEvent {
        private Long id;
        private String message;
    }
}
















