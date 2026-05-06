package com.vpm.taskserver.integration.service;

import com.vpm.taskserver.config.IntegrationTestsDBConfig;
import com.vpm.taskserver.config.IntegrationTestsRabbitMQConfig;
import com.vpm.taskserver.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
    private AmqpAdmin amqpAdmin;

    @Autowired
    private ConnectionFactory connectionFactory;

    private SimpleMessageListenerContainer container;
    private List<Message> receivedMessages;
    private CountDownLatch latch;

    private static final String TEST_TASK_QUEUE = "queue.volunteer-task";
    private static final String TEST_TASK_EXCHANGE = "exchange.volunteer-task";
    private static final String  TEST_TASK_ROUTING_KEY = "volunteer-task.assigned";

    private static final String TEST_SUGGESTION_QUEUE = "queue.volunteer-suggestion";
    private static final String TEST_SUGGESTION_EXCHANGE = "exchange.volunteer-suggestion";
    private static final String  TEST_SUGGESTION_ROUTING_KEY = "volunteer-suggestion.reported";

    @BeforeEach
    public void setUp() {
        receivedMessages = new ArrayList<>();
        latch = new CountDownLatch(1);
        setupMessageListener();
    }

    private void setupMessageListener() {
        container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(TEST_TASK_QUEUE, TEST_SUGGESTION_QUEUE);
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

}
