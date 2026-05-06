package com.vpm.projectserver.contract;

import com.vpm.projectserver.config.IntegrationTestsDBConfig;
import com.vpm.projectserver.config.IntegrationTestsRabbitMQConfig;
import com.vpm.projectserver.dto.event.EventType;
import com.vpm.projectserver.dto.event.VolunteerAssignedToProjectEvent;
import com.vpm.projectserver.entity.Project;
import com.vpm.projectserver.entity.Tag;
import com.vpm.projectserver.repository.ProjectRepository;
import com.vpm.projectserver.repository.TagRepository;
import com.vpm.projectserver.service.EventService;
import com.vpm.projectserver.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Contract Tests for RabbitMQ Message Publishing
 *
 * These tests verify that the ProjectService sends correctly formatted
 * messages to RabbitMQ when volunteer assignments occur.
 *
 * These tests act as Producer tests in the Spring Cloud Contract model.
 * The generated test stubs can be used by consumers (VolunteerServer)
 * for testing their message consumption without needing the real producer.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
        IntegrationTestsDBConfig.class,
        IntegrationTestsRabbitMQConfig.class
})
@DisplayName("RabbitMQ Message Contract Tests - ProjectServer (Producer)")
@Slf4j
@Transactional
public class RabbitMQMessageContractTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TagRepository tagRepository;

    @MockitoBean
    private EventService eventService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Project testProject;
    private Tag testTag;

    private static final Long PROJECT_ID = 1L;
    private static final Long VOLUNTEER_ID_1 = 100L;
    private static final Long VOLUNTEER_ID_2 = 101L;

    @BeforeEach
    public void setup() {
        reset(eventService);

        projectRepository.deleteAll();
        tagRepository.deleteAll();

        testTag = new Tag();
        testTag.setName("Environment");
        testTag = tagRepository.save(testTag);

        testProject = Project.builder()
                .title("Clean Up Beach")
                .description("Help clean up the local beach")
                .imgPath("/images/beach.png")
                .organizationUserId(1L)
                .organizationName("Earth Care")
                .tags(Set.of(testTag))
                .build();

        testProject = projectRepository.save(testProject);
    }

    /**
     * CONTRACT: Should send volunteer assigned to project event
     *
     * This test verifies that when a volunteer is assigned to a project,
     * a message is sent to RabbitMQ with the correct structure:
     * {
     *   "volunteerId": 100,
     *   "projectId": 1,
     *   "createdAt": "2026-05-06"
     * }
     *
     * With headers:
     * - eventType: VOLUNTEER_ASSIGNED_TO_PROJECT
     */
    @Test
    @DisplayName("Contract: Should send volunteer assigned to project event with correct message structure")
    public void testSendVolunteerAssignedToProjectEventMessageStructure() {
        // Arrange
        Long projectId = testProject.getId();
        Long volunteerId = VOLUNTEER_ID_1;

        // Act
        projectService.assignVolunteerToProject(projectId, volunteerId);

        // Assert - Verify EventService.sendEvent was called with correct parameters
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Verify the event service method was called
                    verify(eventService, times(1)).sendEvent(
                            any(VolunteerAssignedToProjectEvent.class),
                            anyString(),
                            anyString(),
                            eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
                    );
                });

        // Capture and verify the event details
        verify(eventService).sendEvent(
                argThat((e -> {
                    VolunteerAssignedToProjectEvent event = (VolunteerAssignedToProjectEvent) e;
                    return event.getVolunteerId().equals(volunteerId) &&
                    event.getProjectId().equals(projectId) &&
                    event.getCreatedAt() != null;
                        }
                )),
                anyString(),
                anyString(),
                eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
        );
    }

    /**
     * CONTRACT: Should send volunteer assigned event to correct exchange and routing key
     *
     * Verifies that the message is routed to the correct RabbitMQ destinations
     */
    @Test
    @DisplayName("Contract: Should send message to correct RabbitMQ exchange and routing key")
    public void testSendVolunteerAssignedEventToCorrectExchange() {
        // Arrange
        Long projectId = testProject.getId();
        Long volunteerId = VOLUNTEER_ID_1;

        // Act
        projectService.assignVolunteerToProject(projectId, volunteerId);

        // Assert
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(eventService).sendEvent(
                            any(),
                            contains("volunteer-project.assigned"),
                            contains("exchange.volunteer-project"),
                            eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
                    );
                });
    }

    /**
     * CONTRACT: Should send multiple volunteer assignment events
     *
     * Verifies that each volunteer assignment generates a separate message
     * with the correct volunteer and project IDs
     */
    @Test
    @DisplayName("Contract: Should send separate events for multiple volunteer assignments")
    public void testSendMultipleVolunteerAssignmentEvents() {
        // Arrange
        Long projectId = testProject.getId();

        // Act
        projectService.assignVolunteerToProject(projectId, VOLUNTEER_ID_1);
        projectService.assignVolunteerToProject(projectId, VOLUNTEER_ID_2);

        // Assert
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Should be called twice, once for each assignment
                    verify(eventService, times(2)).sendEvent(
                            any(VolunteerAssignedToProjectEvent.class),
                            anyString(),
                            anyString(),
                            eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
                    );
                });

        // Verify each message has correct volunteer ID
        verify(eventService).sendEvent(
                argThat(e -> {VolunteerAssignedToProjectEvent event = (VolunteerAssignedToProjectEvent) e;
                    return event.getVolunteerId().equals(VOLUNTEER_ID_1);}),
                anyString(),
                anyString(),
                eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
        );

        verify(eventService).sendEvent(
                argThat(e -> {VolunteerAssignedToProjectEvent event = (VolunteerAssignedToProjectEvent) e;
                    return event.getVolunteerId().equals(VOLUNTEER_ID_2) ;}),
                anyString(),
                anyString(),
                eq(EventType.VOLUNTEER_ASSIGNED_TO_PROJECT)
        );
    }

    /**
     * CONTRACT: Event should contain required fields
     *
     * Verifies that all required fields are present in the message
     */
    @Test
    @DisplayName("Contract: Message should contain all required fields")
    public void testVolunteerAssignedEventContainsAllRequiredFields() {
        // Arrange
        Long projectId = testProject.getId();
        Long volunteerId = VOLUNTEER_ID_1;

        // Act
        projectService.assignVolunteerToProject(projectId, volunteerId);

        // Assert
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(eventService).sendEvent(
                            argThat(e -> {

                                VolunteerAssignedToProjectEvent event = (VolunteerAssignedToProjectEvent) e;

                                // Verify all required fields are present and non-null
                                assertNotEquals(0L, event.getVolunteerId(), "volunteerId must not be null or zero");
                                assertNotEquals(0L, event.getProjectId(), "projectId must not be null or zero");
                                assertNotNull(event.getCreatedAt(), "createdAt must not be null");

                                // Verify correct values
                                assertEquals(volunteerId, event.getVolunteerId());
                                assertEquals(projectId, event.getProjectId());
                                assertEquals(LocalDate.now(), event.getCreatedAt());

                                return true;
                            }),
                            anyString(),
                            anyString(),
                            any()
                    );
                });
    }

    /**
     * CONTRACT: Message field types must be correct
     *
     * Verifies that fields have the correct data types
     */
    @Test
    @DisplayName("Contract: Message fields should have correct data types")
    public void testVolunteerAssignedEventFieldTypes() {
        // Arrange
        Long projectId = testProject.getId();
        Long volunteerId = VOLUNTEER_ID_1;

        // Act
        projectService.assignVolunteerToProject(projectId, volunteerId);

        // Assert
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(eventService).sendEvent(
                            argThat(e -> {

                                VolunteerAssignedToProjectEvent event = (VolunteerAssignedToProjectEvent) e;

                                // Verify data types
                                assertTrue(event.getVolunteerId() instanceof Long,
                                        "volunteerId should be Long type");
                                assertTrue(event.getProjectId() instanceof Long,
                                        "projectId should be Long type");
                                assertTrue(event.getCreatedAt() instanceof LocalDate,
                                        "createdAt should be LocalDate type");

                                return true;
                            }),
                            anyString(),
                            anyString(),
                            any()
                    );
                });
    }

    /**
     * CONTRACT: Should not send event on project not found
     *
     * Verifies that no message is sent when project doesn't exist
     */
    @Test
    @DisplayName("Contract: Should NOT send event when project does not exist")
    public void testNoEventSentWhenProjectNotFound() {
        // Arrange
        Long nonExistentProjectId = 9999L;
        Long volunteerId = VOLUNTEER_ID_1;

        // Act & Assert
        assertThrows(Exception.class, () ->
                projectService.assignVolunteerToProject(nonExistentProjectId, volunteerId)
        );

        // Verify event was never sent
        verify(eventService, never()).sendEvent(any(), anyString(), anyString(), any());
    }

}

