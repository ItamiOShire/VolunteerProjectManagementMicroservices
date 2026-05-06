package com.vpm.volunteerserver.integration.service;

import com.vpm.volunteerserver.config.IntegrationTestsDBConfig;
import com.vpm.volunteerserver.dto.event.VolunteerAssignedToProjectEvent;
import com.vpm.volunteerserver.dto.event.VolunteerAssignedToTaskEvent;
import com.vpm.volunteerserver.dto.event.VolunteerReportedTaskSuggestionEvent;
import com.vpm.volunteerserver.entity.TaskSuggestion;
import com.vpm.volunteerserver.entity.Volunteer;
import com.vpm.volunteerserver.entity.VolunteerProject;
import com.vpm.volunteerserver.entity.VolunteerTask;
import com.vpm.volunteerserver.exception.volunteer.NoSuchVolunteerException;
import com.vpm.volunteerserver.repository.TaskSuggestionRepository;
import com.vpm.volunteerserver.repository.VolunteerProjectRepository;
import com.vpm.volunteerserver.repository.VolunteerRepository;
import com.vpm.volunteerserver.repository.VolunteerTaskRepository;
import com.vpm.volunteerserver.service.event.VolunteerEventService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(IntegrationTestsDBConfig.class)
@Transactional
@Slf4j
@DisplayName("VolunteerEventService Integration Tests")
public class VolunteerEventServiceTest {

    @Autowired
    private VolunteerEventService volunteerEventService;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private VolunteerProjectRepository volunteerProjectRepository;

    @Autowired
    private VolunteerTaskRepository volunteerTaskRepository;

    @Autowired
    private TaskSuggestionRepository taskSuggestionRepository;

    private Volunteer testVolunteer;
    private Volunteer testVolunteer2;

    private static final Long VOLUNTEER_ID = 1L;
    private static final Long VOLUNTEER_ID_2 = 2L;
    private static final Long PROJECT_ID = 100L;
    private static final Long TASK_ID = 200L;
    private static final Long NONEXISTENT_VOLUNTEER_ID = 999L;

    @BeforeEach
    void setUp() {
        // Clean up in correct order (dependent first) with flush() for DB sync
        taskSuggestionRepository.deleteAll();
        taskSuggestionRepository.flush();

        volunteerTaskRepository.deleteAll();
        volunteerTaskRepository.flush();

        volunteerProjectRepository.deleteAll();
        volunteerProjectRepository.flush();

        volunteerRepository.deleteAll();
        volunteerRepository.flush();

        // Create test volunteers
        testVolunteer = Volunteer.builder()
                .userId(VOLUNTEER_ID)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phoneNumber("555-1234")
                .contactEmail("john@example.com")
                .volunteerProjects(new ArrayList<>())
                .volunteerTasks(new ArrayList<>())
                .volunteerTaskSuggestions(new ArrayList<>())
                .build();

        testVolunteer2 = Volunteer.builder()
                .userId(VOLUNTEER_ID_2)
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1995, 3, 20))
                .phoneNumber("555-9999")
                .contactEmail("jane@example.com")
                .volunteerProjects(new ArrayList<>())
                .volunteerTasks(new ArrayList<>())
                .volunteerTaskSuggestions(new ArrayList<>())
                .build();
    }

    @AfterEach
    void tearDown() {
        taskSuggestionRepository.deleteAll();
        volunteerTaskRepository.deleteAll();
        volunteerProjectRepository.deleteAll();
        volunteerRepository.deleteAll();
    }

    @Nested
    @DisplayName("assignVolunteerToProject tests")
    class AssignVolunteerToProjectTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully assign volunteer to project")
            void shouldSuccessfullyAssignVolunteerToProject() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerAssignedToProjectEvent event = new VolunteerAssignedToProjectEvent();
                event.setVolunteerId(saved.getUserId());
                event.setProjectId(PROJECT_ID);
                event.setAssignedDate(LocalDate.now());

                VolunteerProject result = volunteerEventService.assignVolunteerToProject(event);

                assertNotNull(result);
                assertEquals(saved.getUserId(), result.getVolunteerProjectId().getVolunteerUserId());
                assertEquals(PROJECT_ID, result.getVolunteerProjectId().getProjectId());
                assertEquals(PROJECT_ID, result.getProjectId());
                assertNotNull(result.getVolunteer());
            }

            @Test
            @DisplayName("Should persist volunteer project to database")
            void shouldPersistVolunteerProjectToDatabase() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerAssignedToProjectEvent event = new VolunteerAssignedToProjectEvent();
                event.setVolunteerId(saved.getUserId());
                event.setProjectId(PROJECT_ID);
                event.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToProject(event);

                List<VolunteerProject> projects = volunteerProjectRepository.findAll();
                assertEquals(1, projects.size(), "Should have one project in database");

                VolunteerProject retrievedProject = projects.get(0);
                assertEquals(saved.getUserId(), retrievedProject.getVolunteerProjectId().getVolunteerUserId());
                assertEquals(PROJECT_ID, retrievedProject.getProjectId());
            }

            @Test
            @DisplayName("Should update volunteer projects collection")
            void shouldUpdateVolunteerProjectsCollection() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerAssignedToProjectEvent event = new VolunteerAssignedToProjectEvent();
                event.setVolunteerId(saved.getUserId());
                event.setProjectId(PROJECT_ID);
                event.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToProject(event);

                Volunteer updatedVolunteer = volunteerRepository.findByUserId(saved.getUserId()).orElse(null);
                assertNotNull(updatedVolunteer);
                assertEquals(1, updatedVolunteer.getVolunteerProjects().size());
                assertEquals(PROJECT_ID, updatedVolunteer.getVolunteerProjects().get(0).getProjectId());
            }

            @Test
            @DisplayName("Should assign multiple volunteers to same project")
            void shouldAssignMultipleVolunteersToSameProject() {
                volunteerRepository.saveAll(List.of(testVolunteer, testVolunteer2));

                VolunteerAssignedToProjectEvent event1 = new VolunteerAssignedToProjectEvent();
                event1.setVolunteerId(VOLUNTEER_ID);
                event1.setProjectId(PROJECT_ID);
                event1.setAssignedDate(LocalDate.now());

                VolunteerAssignedToProjectEvent event2 = new VolunteerAssignedToProjectEvent();
                event2.setVolunteerId(VOLUNTEER_ID_2);
                event2.setProjectId(PROJECT_ID);
                event2.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToProject(event1);
                volunteerEventService.assignVolunteerToProject(event2);

                List<VolunteerProject> projects = volunteerProjectRepository.findAll();
                assertEquals(2, projects.size(), "Should have two assignments");

                long projectCount = projects.stream()
                        .filter(vp -> vp.getProjectId() == PROJECT_ID)
                        .count();
                assertEquals(2, projectCount, "Both should be assigned to same project");
            }

            @Test
            @DisplayName("Should assign same volunteer to different projects")
            void shouldAssignSameVolunteerToDifferentProjects() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerAssignedToProjectEvent event1 = new VolunteerAssignedToProjectEvent();
                event1.setVolunteerId(saved.getUserId());
                event1.setProjectId(PROJECT_ID);
                event1.setAssignedDate(LocalDate.now());

                VolunteerAssignedToProjectEvent event2 = new VolunteerAssignedToProjectEvent();
                event2.setVolunteerId(saved.getUserId());
                event2.setProjectId(PROJECT_ID + 1);
                event2.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToProject(event1);
                volunteerEventService.assignVolunteerToProject(event2);

                Volunteer updatedVolunteer = volunteerRepository.findByUserId(saved.getUserId()).orElse(null);
                assertNotNull(updatedVolunteer);
                assertEquals(2, updatedVolunteer.getVolunteerProjects().size());
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should throw NoSuchVolunteerException when volunteer not found")
            void shouldThrowExceptionWhenVolunteerNotFound() {
                VolunteerAssignedToProjectEvent event = new VolunteerAssignedToProjectEvent();
                event.setVolunteerId(NONEXISTENT_VOLUNTEER_ID);
                event.setProjectId(PROJECT_ID);
                event.setAssignedDate(LocalDate.now());

                assertThrows(NoSuchVolunteerException.class,
                        () -> volunteerEventService.assignVolunteerToProject(event),
                        "Should throw exception when volunteer not found");
            }
        }
    }

    @Nested
    @DisplayName("assignVolunteerToTask tests")
    class AssignVolunteerToTaskTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully assign volunteer to task")
            void shouldSuccessfullyAssignVolunteerToTask() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerAssignedToTaskEvent event = new VolunteerAssignedToTaskEvent();
                event.setVolunteerId(saved.getUserId());
                event.setTaskId(TASK_ID);
                event.setAssignedDate(LocalDate.now());

                VolunteerTask result = volunteerEventService.assignVolunteerToTask(event);

                assertNotNull(result);
                assertEquals(saved.getUserId(), result.getVolunteerTaskId().getVolunteerUserId());
                assertEquals(TASK_ID, result.getVolunteerTaskId().getTaskId());
                assertEquals(TASK_ID, result.getTaskId());
                assertNotNull(result.getVolunteer());
            }

            @Test
            @DisplayName("Should persist volunteer task to database")
            void shouldPersistVolunteerTaskToDatabase() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerAssignedToTaskEvent event = new VolunteerAssignedToTaskEvent();
                event.setVolunteerId(saved.getUserId());
                event.setTaskId(TASK_ID);
                event.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToTask(event);

                List<VolunteerTask> tasks = volunteerTaskRepository.findAll();
                assertEquals(1, tasks.size(), "Should have one task in database");

                VolunteerTask retrievedTask = tasks.get(0);
                assertEquals(saved.getUserId(), retrievedTask.getVolunteerTaskId().getVolunteerUserId());
                assertEquals(TASK_ID, retrievedTask.getTaskId());
            }

            @Test
            @DisplayName("Should update volunteer tasks collection")
            void shouldUpdateVolunteerTasksCollection() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerAssignedToTaskEvent event = new VolunteerAssignedToTaskEvent();
                event.setVolunteerId(saved.getUserId());
                event.setTaskId(TASK_ID);
                event.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToTask(event);

                Volunteer updatedVolunteer = volunteerRepository.findByUserId(saved.getUserId()).orElse(null);
                assertNotNull(updatedVolunteer);
                assertEquals(1, updatedVolunteer.getVolunteerTasks().size());
                assertEquals(TASK_ID, updatedVolunteer.getVolunteerTasks().get(0).getTaskId());
            }

            @Test
            @DisplayName("Should assign multiple volunteers to same task")
            void shouldAssignMultipleVolunteersToSameTask() {
                volunteerRepository.saveAll(List.of(testVolunteer, testVolunteer2));

                VolunteerAssignedToTaskEvent event1 = new VolunteerAssignedToTaskEvent();
                event1.setVolunteerId(VOLUNTEER_ID);
                event1.setTaskId(TASK_ID);
                event1.setAssignedDate(LocalDate.now());

                VolunteerAssignedToTaskEvent event2 = new VolunteerAssignedToTaskEvent();
                event2.setVolunteerId(VOLUNTEER_ID_2);
                event2.setTaskId(TASK_ID);
                event2.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToTask(event1);
                volunteerEventService.assignVolunteerToTask(event2);

                List<VolunteerTask> tasks = volunteerTaskRepository.findAll();
                assertEquals(2, tasks.size(), "Should have two assignments");

                long taskCount = tasks.stream()
                        .filter(vt -> vt.getTaskId() == TASK_ID)
                        .count();
                assertEquals(2, taskCount, "Both should be assigned to same task");
            }

            @Test
            @DisplayName("Should assign same volunteer to different tasks")
            void shouldAssignSameVolunteerToDifferentTasks() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerAssignedToTaskEvent event1 = new VolunteerAssignedToTaskEvent();
                event1.setVolunteerId(saved.getUserId());
                event1.setTaskId(TASK_ID);
                event1.setAssignedDate(LocalDate.now());

                VolunteerAssignedToTaskEvent event2 = new VolunteerAssignedToTaskEvent();
                event2.setVolunteerId(saved.getUserId());
                event2.setTaskId(TASK_ID + 1);
                event2.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToTask(event1);
                volunteerEventService.assignVolunteerToTask(event2);

                Volunteer updatedVolunteer = volunteerRepository.findByUserId(saved.getUserId()).orElse(null);
                assertNotNull(updatedVolunteer);
                assertEquals(2, updatedVolunteer.getVolunteerTasks().size());
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should throw NoSuchVolunteerException when volunteer not found")
            void shouldThrowExceptionWhenVolunteerNotFound() {
                VolunteerAssignedToTaskEvent event = new VolunteerAssignedToTaskEvent();
                event.setVolunteerId(NONEXISTENT_VOLUNTEER_ID);
                event.setTaskId(TASK_ID);
                event.setAssignedDate(LocalDate.now());

                assertThrows(NoSuchVolunteerException.class,
                        () -> volunteerEventService.assignVolunteerToTask(event),
                        "Should throw exception when volunteer not found");
            }
        }
    }

    @Nested
    @DisplayName("assignVolunteerToSuggestedTask tests")
    class AssignVolunteerToSuggestedTaskTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully assign volunteer to suggested task")
            void shouldSuccessfullyAssignVolunteerToSuggestedTask() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerReportedTaskSuggestionEvent event = new VolunteerReportedTaskSuggestionEvent();
                event.setVolunteerId(saved.getUserId());
                event.setTaskId(TASK_ID);
                event.setAssignedDate(LocalDate.now());

                TaskSuggestion result = volunteerEventService.assignVolunteerToSuggestedTask(event);

                assertNotNull(result);
                assertEquals(saved.getUserId(), result.getTaskSuggestionId().getVolunteerUserId());
                assertEquals(TASK_ID, result.getTaskSuggestionId().getTaskId());
                assertEquals(TASK_ID, result.getTaskId());
                assertNotNull(result.getVolunteer());
            }

            @Test
            @DisplayName("Should persist task suggestion to database")
            void shouldPersistTaskSuggestionToDatabase() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerReportedTaskSuggestionEvent event = new VolunteerReportedTaskSuggestionEvent();
                event.setVolunteerId(saved.getUserId());
                event.setTaskId(TASK_ID);
                event.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToSuggestedTask(event);

                List<TaskSuggestion> suggestions = taskSuggestionRepository.findAll();
                assertEquals(1, suggestions.size(), "Should have one suggestion in database");

                TaskSuggestion retrievedSuggestion = suggestions.get(0);
                assertEquals(saved.getUserId(), retrievedSuggestion.getTaskSuggestionId().getVolunteerUserId());
                assertEquals(TASK_ID, retrievedSuggestion.getTaskId());
            }

            @Test
            @DisplayName("Should update volunteer task suggestions collection")
            void shouldUpdateVolunteerTaskSuggestionsCollection() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerReportedTaskSuggestionEvent event = new VolunteerReportedTaskSuggestionEvent();
                event.setVolunteerId(saved.getUserId());
                event.setTaskId(TASK_ID);
                event.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToSuggestedTask(event);

                Volunteer updatedVolunteer = volunteerRepository.findByUserId(saved.getUserId()).orElse(null);
                assertNotNull(updatedVolunteer);
                assertEquals(1, updatedVolunteer.getVolunteerTaskSuggestions().size());
                assertEquals(TASK_ID, updatedVolunteer.getVolunteerTaskSuggestions().get(0).getTaskId());
            }

            @Test
            @DisplayName("Should accept multiple task suggestions from same volunteer")
            void shouldAcceptMultipleTaskSuggestionsFromSameVolunteer() {
                Volunteer saved = volunteerRepository.save(testVolunteer);

                VolunteerReportedTaskSuggestionEvent event1 = new VolunteerReportedTaskSuggestionEvent();
                event1.setVolunteerId(saved.getUserId());
                event1.setTaskId(TASK_ID);
                event1.setAssignedDate(LocalDate.now());

                VolunteerReportedTaskSuggestionEvent event2 = new VolunteerReportedTaskSuggestionEvent();
                event2.setVolunteerId(saved.getUserId());
                event2.setTaskId(TASK_ID + 1);
                event2.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToSuggestedTask(event1);
                volunteerEventService.assignVolunteerToSuggestedTask(event2);

                Volunteer updatedVolunteer = volunteerRepository.findByUserId(saved.getUserId()).orElse(null);
                assertNotNull(updatedVolunteer);
                assertEquals(2, updatedVolunteer.getVolunteerTaskSuggestions().size());
            }

            @Test
            @DisplayName("Should accept suggestions from multiple volunteers")
            void shouldAcceptSuggestionsFromMultipleVolunteers() {
                volunteerRepository.saveAll(List.of(testVolunteer, testVolunteer2));

                VolunteerReportedTaskSuggestionEvent event1 = new VolunteerReportedTaskSuggestionEvent();
                event1.setVolunteerId(VOLUNTEER_ID);
                event1.setTaskId(TASK_ID);
                event1.setAssignedDate(LocalDate.now());

                VolunteerReportedTaskSuggestionEvent event2 = new VolunteerReportedTaskSuggestionEvent();
                event2.setVolunteerId(VOLUNTEER_ID_2);
                event2.setTaskId(TASK_ID);
                event2.setAssignedDate(LocalDate.now());

                volunteerEventService.assignVolunteerToSuggestedTask(event1);
                volunteerEventService.assignVolunteerToSuggestedTask(event2);

                List<TaskSuggestion> suggestions = taskSuggestionRepository.findAll();
                assertEquals(2, suggestions.size(), "Should have two suggestions");

                long taskCount = suggestions.stream()
                        .filter(ts -> ts.getTaskId() == TASK_ID)
                        .count();
                assertEquals(2, taskCount, "Both should have suggested same task");
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should throw NoSuchVolunteerException when volunteer not found")
            void shouldThrowExceptionWhenVolunteerNotFound() {
                VolunteerReportedTaskSuggestionEvent event = new VolunteerReportedTaskSuggestionEvent();
                event.setVolunteerId(NONEXISTENT_VOLUNTEER_ID);
                event.setTaskId(TASK_ID);
                event.setAssignedDate(LocalDate.now());

                assertThrows(NoSuchVolunteerException.class,
                        () -> volunteerEventService.assignVolunteerToSuggestedTask(event),
                        "Should throw exception when volunteer not found");
            }
        }
    }

}
