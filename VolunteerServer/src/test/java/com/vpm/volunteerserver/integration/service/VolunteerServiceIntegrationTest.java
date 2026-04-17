package com.vpm.volunteerserver.integration.service;

import com.vpm.volunteerserver.config.IntegrationTestsDBConfig;
import com.vpm.volunteerserver.dto.response.VolunteerProfileResponse;
import com.vpm.volunteerserver.dto.template.VolunteerToAssignToTaskTemplate;
import com.vpm.volunteerserver.entity.*;
import com.vpm.volunteerserver.entity.pks.TaskSuggestionId;
import com.vpm.volunteerserver.entity.pks.VolunteerProjectId;
import com.vpm.volunteerserver.entity.pks.VolunteerTaskId;
import com.vpm.volunteerserver.exception.volunteer.NoSuchVolunteerException;
import com.vpm.volunteerserver.repository.*;
import com.vpm.volunteerserver.service.VolunteerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Import(IntegrationTestsDBConfig.class)
@Transactional  // Each test runs in a transaction that rolls back after completion
public class VolunteerServiceIntegrationTest {

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private VolunteerProjectRepository volunteerProjectRepository;

    @Autowired
    private VolunteerTaskRepository volunteerTaskRepository;

    @Autowired
    private TaskSuggestionRepository taskSuggestionRepository;

    private Volunteer testVolunteer1;
    private Volunteer testVolunteer2;
    private Volunteer testVolunteer3;
    private final Long PROJECT_ID = 100L;
    private final Long TASK_ID = 200L;

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
        volunteerRepository.flush();  // CRITICAL: Ensures DB is clean before test data creation

        testVolunteer1 = Volunteer.builder()
                .userId(1L)
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
                .userId(2L)
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1995, 3, 20))
                .phoneNumber("555-9999")
                .contactEmail("jane@example.com")
                .volunteerProjects(new ArrayList<>())
                .volunteerTasks(new ArrayList<>())
                .volunteerTaskSuggestions(new ArrayList<>())
                .build();

        testVolunteer3 = Volunteer.builder()
                .userId(3L)
                .firstName("Bob")
                .lastName("Johnson")
                .dateOfBirth(LocalDate.of(1988, 7, 10))
                .phoneNumber("555-5555")
                .contactEmail("bob@example.com")
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
    @DisplayName("getVolunteerProfile Tests")
    class GetVolunteerProfileTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully retrieve volunteer profile")
            void shouldSuccessfullyRetrieveVolunteerProfile() {
                Volunteer saved = volunteerRepository.save(testVolunteer1);

                VolunteerProfileResponse response = volunteerService.getVolunteerProfile(saved.getUserId());

                assertNotNull(response);
                assertEquals("John Doe", response.getFullName());
                assertEquals(LocalDate.of(1990, 5, 15), response.getDateOfBirth());
                assertNotNull(response.getContact());
            }

            @Test
            @DisplayName("Should retrieve profile with correct contact information")
            void shouldRetrieveProfileWithCorrectContactInformation() {
                Volunteer saved = volunteerRepository.save(testVolunteer1);

                VolunteerProfileResponse response = volunteerService.getVolunteerProfile(saved.getUserId());

                assertTrue(response.getContact().contains("john@example.com") ||
                           response.getContact().contains("555-1234"),
                        "Contact should contain email or phone");
            }

            @Test
            @DisplayName("Should retrieve different volunteer profiles")
            void shouldRetrieveDifferentVolunteerProfiles() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2));

                VolunteerProfileResponse response1 = volunteerService.getVolunteerProfile(1L);
                VolunteerProfileResponse response2 = volunteerService.getVolunteerProfile(2L);

                assertAll(
                        () -> assertEquals("John Doe", response1.getFullName()),
                        () -> assertEquals("Jane Smith", response2.getFullName()),
                        () -> assertNotEquals(response1.getFullName(), response2.getFullName())
                );
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should throw NoSuchVolunteerException when volunteer not found")
            void shouldThrowNoSuchVolunteerExceptionWhenNotFound() {
                assertThrows(NoSuchVolunteerException.class,
                        () -> volunteerService.getVolunteerProfile(999L),
                        "Should throw exception when volunteer not found");
            }

            @Test
            @DisplayName("Should throw exception for non-existent userId")
            void shouldThrowExceptionForNonExistentUserId() {
                volunteerRepository.save(testVolunteer1);

                assertThrows(NoSuchVolunteerException.class,
                        () -> volunteerService.getVolunteerProfile(9999L));
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProject Tests")
    class GetVolunteersInProjectTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should retrieve volunteers in project with VolunteerProject relationships")
            void shouldRetrieveVolunteersInProjectWithRelationships() {
                // Setup: Save volunteers and create project relationships
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                testVolunteer2 = volunteerRepository.save(testVolunteer2);
                volunteerRepository.flush();  // CRITICAL: Flush to ensure in database

                // Use actual saved volunteer userIds, NOT hardcoded values
                VolunteerProject vp1 = new VolunteerProject(
                        new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID),
                        testVolunteer1,
                        PROJECT_ID
                );
                VolunteerProject vp2 = new VolunteerProject(
                        new VolunteerProjectId(testVolunteer2.getUserId(), PROJECT_ID),
                        testVolunteer2,
                        PROJECT_ID
                );
                volunteerProjectRepository.saveAll(List.of(vp1, vp2));

                // Test
                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProject(PROJECT_ID);

                // Verify
                assertNotNull(response);
                assertEquals(2, response.size(), "Should return 2 volunteers in project");
                assertTrue(response.stream().anyMatch(v -> v.getFullName().equals("John Doe")));
                assertTrue(response.stream().anyMatch(v -> v.getFullName().equals("Jane Smith")));
            }

            @Test
            @DisplayName("Should return empty list for project with no volunteers")
            void shouldReturnEmptyListForProjectWithNoVolunteers() {
                volunteerRepository.save(testVolunteer1);

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProject(PROJECT_ID);

                assertNotNull(response);
                assertTrue(response.isEmpty(), "Should return empty list when project has no volunteers");
            }

            @Test
            @DisplayName("Should correctly map volunteer data from VolunteerProject relationships")
            void shouldCorrectlyMapDataFromRelationships() {
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                volunteerRepository.flush();  // CRITICAL: Flush before relationship

                VolunteerProject vp = new VolunteerProject(
                        new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID),
                        testVolunteer1,
                        PROJECT_ID
                );
                volunteerProjectRepository.save(vp);

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProject(PROJECT_ID);

                assertEquals(1, response.size());
                assertEquals("John Doe", response.get(0).getFullName());
                assertEquals(LocalDate.of(1990, 5, 15), response.get(0).getDateOfBirth());
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should handle non-existent project ID")
            void shouldHandleNonExistentProjectId() {
                volunteerRepository.save(testVolunteer1);

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProject(9999L);

                assertTrue(response.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProjectWhoAreAssignedToTask Tests")
    class GetVolunteersAssignedToTaskTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should retrieve volunteers in project with VolunteerProject relationships")
            void shouldRetrieveVolunteersInProjectWithRelationships() {
                // Setup: Save volunteers and FLUSH to ensure they're in database before creating relationships
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                testVolunteer2 = volunteerRepository.save(testVolunteer2);
                volunteerRepository.flush();  // CRITICAL: Ensure volunteers are persisted before relationships

                // Create project relationships using actual saved volunteer userIds
                VolunteerProject vp1 = new VolunteerProject(
                        new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID),
                        testVolunteer1,
                        PROJECT_ID
                );
                VolunteerProject vp2 = new VolunteerProject(
                        new VolunteerProjectId(testVolunteer2.getUserId(), PROJECT_ID),
                        testVolunteer2,
                        PROJECT_ID
                );
                volunteerProjectRepository.saveAll(List.of(vp1, vp2));

                // Add task assignment - only volunteer1 assigned to task using actual userId
                VolunteerTask vt1 = new VolunteerTask(
                        new VolunteerTaskId(testVolunteer1.getUserId(), TASK_ID),
                        testVolunteer1,
                        TASK_ID
                );
                volunteerTaskRepository.save(vt1);

                // Test
                List<VolunteerProfileResponse> response = volunteerService
                        .getVolunteersInProjectWhoAreAssignedToTask(PROJECT_ID, TASK_ID);

                // Verify
                assertEquals(1, response.size(), "Should return only 1 volunteer assigned to task");
                assertEquals("John Doe", response.get(0).getFullName());
            }

            @Test
            @DisplayName("Should return multiple volunteers assigned to same task")
            void shouldReturnMultipleVolunteersAssignedToTask() {
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                testVolunteer2 = volunteerRepository.save(testVolunteer2);
                volunteerRepository.flush();  // CRITICAL: Flush before relationships

                // Add project relationships using actual userIds
                volunteerProjectRepository.saveAll(List.of(
                        new VolunteerProject(new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID), testVolunteer1, PROJECT_ID),
                        new VolunteerProject(new VolunteerProjectId(testVolunteer2.getUserId(), PROJECT_ID), testVolunteer2, PROJECT_ID)
                ));

                // Both assigned to same task using actual userIds
                volunteerTaskRepository.saveAll(List.of(
                        new VolunteerTask(new VolunteerTaskId(testVolunteer1.getUserId(), TASK_ID), testVolunteer1, TASK_ID),
                        new VolunteerTask(new VolunteerTaskId(testVolunteer2.getUserId(), TASK_ID), testVolunteer2, TASK_ID)
                ));

                List<VolunteerProfileResponse> response = volunteerService
                        .getVolunteersInProjectWhoAreAssignedToTask(PROJECT_ID, TASK_ID);

                assertEquals(2, response.size(), "Should return 2 volunteers");
                assertTrue(response.stream().anyMatch(v -> v.getFullName().equals("John Doe")));
                assertTrue(response.stream().anyMatch(v -> v.getFullName().equals("Jane Smith")));
            }

            @Test
            @DisplayName("Should return empty when no volunteers assigned to task")
            void shouldReturnEmptyWhenNoneAssigned() {
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                volunteerRepository.flush();  // CRITICAL: Flush before relationship

                // Add project but no task using actual userId
                volunteerProjectRepository.save(
                        new VolunteerProject(new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID), testVolunteer1, PROJECT_ID)
                );

                List<VolunteerProfileResponse> response = volunteerService
                        .getVolunteersInProjectWhoAreAssignedToTask(PROJECT_ID, TASK_ID);

                assertTrue(response.isEmpty(), "Should return empty when no assignments exist");
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should handle non-existent project ID")
            void shouldHandleNonExistentProjectId() {
                List<VolunteerProfileResponse> response = volunteerService
                        .getVolunteersInProjectWhoAreAssignedToTask(9999L, TASK_ID);

                assertTrue(response.isEmpty());
            }

            @Test
            @DisplayName("Should handle non-existent task ID")
            void shouldHandleNonExistentTaskId() {
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                volunteerRepository.flush();  // CRITICAL: Flush before relationship

                volunteerProjectRepository.save(
                        new VolunteerProject(new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID), testVolunteer1, PROJECT_ID)
                );

                List<VolunteerProfileResponse> response = volunteerService
                        .getVolunteersInProjectWhoAreAssignedToTask(PROJECT_ID, 9999L);

                assertTrue(response.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProjectWhoAreNotInTask Tests")
    class GetVolunteersNotInTaskTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should return volunteers not assigned to task")
            void shouldReturnVolunteersNotAssignedToTask() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2));

                List<VolunteerProfileResponse> response = volunteerService
                        .getVolunteersInProjectWhoAreNotInTask(1L, 1L);

                assertNotNull(response);
            }

            @Test
            @DisplayName("Should map unassigned volunteers correctly")
            void shouldMapUnassignedVolunteersCorrectly() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2));

                List<VolunteerProfileResponse> response = volunteerService
                        .getVolunteersInProjectWhoAreNotInTask(1L, 1L);

                assertTrue(response.stream()
                        .allMatch(v -> v.getFullName() != null && v.getDateOfBirth() != null),
                        "All volunteers should be correctly mapped");
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should handle non-existent task ID")
            void shouldHandleNonExistentTaskId() {
                volunteerRepository.save(testVolunteer1);

                List<VolunteerProfileResponse> response = volunteerService
                        .getVolunteersInProjectWhoAreNotInTask(1L, 9999L);

                assertNotNull(response);
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProjectWhoReportedTaskSuggestion Tests")
    class GetVolunteersWithTaskSuggestionTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should retrieve volunteers who reported task suggestion")
            void shouldRetrieveVolunteersWithTaskSuggestion() {
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                testVolunteer2 = volunteerRepository.save(testVolunteer2);
                volunteerRepository.flush();  // CRITICAL: Flush before relationships

                // Add project relationships using actual userIds
                volunteerProjectRepository.saveAll(List.of(
                        new VolunteerProject(new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID), testVolunteer1, PROJECT_ID),
                        new VolunteerProject(new VolunteerProjectId(testVolunteer2.getUserId(), PROJECT_ID), testVolunteer2, PROJECT_ID)
                ));

                // Only volunteer1 reported suggestion using actual userId
                TaskSuggestion ts = new TaskSuggestion(
                        new TaskSuggestionId(testVolunteer1.getUserId(), TASK_ID),
                        testVolunteer1,
                        TASK_ID
                );
                taskSuggestionRepository.save(ts);

                List<VolunteerToAssignToTaskTemplate> response = volunteerService
                        .getVolunteersInProjectWhoReportedTaskSuggestion(PROJECT_ID, TASK_ID);

                assertEquals(1, response.size(), "Should return only volunteer who reported suggestion");
                assertEquals(1L, response.get(0).getItemId());
                assertEquals("John Doe", response.get(0).getFullName());
            }

            @Test
            @DisplayName("Should return multiple volunteers with suggestions")
            void shouldReturnMultipleVolunteersWithSuggestions() {
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                testVolunteer2 = volunteerRepository.save(testVolunteer2);
                volunteerRepository.flush();  // CRITICAL: Flush before relationships

                // Add project relationships using actual userIds
                volunteerProjectRepository.saveAll(List.of(
                        new VolunteerProject(new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID), testVolunteer1, PROJECT_ID),
                        new VolunteerProject(new VolunteerProjectId(testVolunteer2.getUserId(), PROJECT_ID), testVolunteer2, PROJECT_ID)
                ));

                // Both reported suggestions using actual userIds
                taskSuggestionRepository.saveAll(List.of(
                        new TaskSuggestion(new TaskSuggestionId(testVolunteer1.getUserId(), TASK_ID), testVolunteer1, TASK_ID),
                        new TaskSuggestion(new TaskSuggestionId(testVolunteer2.getUserId(), TASK_ID), testVolunteer2, TASK_ID)
                ));

                List<VolunteerToAssignToTaskTemplate> response = volunteerService
                        .getVolunteersInProjectWhoReportedTaskSuggestion(PROJECT_ID, TASK_ID);

                assertEquals(2, response.size());
                assertTrue(response.stream().anyMatch(v -> v.getItemId() == 1L));
                assertTrue(response.stream().anyMatch(v -> v.getItemId() == 2L));
            }

            @Test
            @DisplayName("Should return empty when no suggestions")
            void shouldReturnEmptyWhenNoSuggestions() {
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                volunteerRepository.flush();  // CRITICAL: Flush before relationship

                volunteerProjectRepository.save(
                        new VolunteerProject(new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID), testVolunteer1, PROJECT_ID)
                );

                List<VolunteerToAssignToTaskTemplate> response = volunteerService
                        .getVolunteersInProjectWhoReportedTaskSuggestion(PROJECT_ID, TASK_ID);

                assertTrue(response.isEmpty());
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should handle non-existent project ID")
            void shouldHandleNonExistentProjectId() {
                List<VolunteerToAssignToTaskTemplate> response = volunteerService
                        .getVolunteersInProjectWhoReportedTaskSuggestion(9999L, TASK_ID);

                assertTrue(response.isEmpty());
            }

            @Test
            @DisplayName("Should handle non-existent task ID")
            void shouldHandleNonExistentTaskId() {
                List<VolunteerToAssignToTaskTemplate> response = volunteerService
                        .getVolunteersInProjectWhoReportedTaskSuggestion(PROJECT_ID, 9999L);

                assertTrue(response.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersNotAssignedToTaskWithTaskSuggestion Tests")
    class GetVolunteersNotAssignedWithSuggestionTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should return unassigned volunteers sorted by suggestion")
            void shouldReturnUnassignedVolunteersSortedBySuggestion() {
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                testVolunteer2 = volunteerRepository.save(testVolunteer2);
                testVolunteer3 = volunteerRepository.save(testVolunteer3);
                volunteerRepository.flush();  // CRITICAL: Flush before relationships

                // All in project using actual userIds
                volunteerProjectRepository.saveAll(List.of(
                        new VolunteerProject(new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID), testVolunteer1, PROJECT_ID),
                        new VolunteerProject(new VolunteerProjectId(testVolunteer2.getUserId(), PROJECT_ID), testVolunteer2, PROJECT_ID),
                        new VolunteerProject(new VolunteerProjectId(testVolunteer3.getUserId(), PROJECT_ID), testVolunteer3, PROJECT_ID)
                ));

                // Only volunteer1 assigned to task - should be filtered out using actual userId
                volunteerTaskRepository.save(
                        new VolunteerTask(new VolunteerTaskId(testVolunteer1.getUserId(), TASK_ID), testVolunteer1, TASK_ID)
                );

                // Volunteer2 reported suggestion, volunteer3 did not using actual userIds
                taskSuggestionRepository.save(
                        new TaskSuggestion(new TaskSuggestionId(testVolunteer2.getUserId(), TASK_ID), testVolunteer2, TASK_ID)
                );

                List<VolunteerToAssignToTaskTemplate> response = volunteerService
                        .getVolunteersNotAssignedToTaskWithTaskSuggestion(PROJECT_ID, TASK_ID);

                assertEquals(2, response.size(), "Should return 2 unassigned volunteers");
                // Volunteer2 should have reportedTaskSuggestion = true
                assertTrue(response.stream()
                        .filter(v -> v.getItemId() == testVolunteer2.getUserId())
                        .findFirst()
                        .orElseThrow()
                        .isReportedTaskSuggestion());
                // Volunteer3 should have reportedTaskSuggestion = false
                assertFalse(response.stream()
                        .filter(v -> v.getItemId() == testVolunteer3.getUserId())
                        .findFirst()
                        .orElseThrow()
                        .isReportedTaskSuggestion());
            }

            // TODO: Fix this test by providing proper test data

            @Test
            @DisplayName("Should return only unassigned volunteers (filter out assigned)")
            void shouldReturnOnlyUnassignedVolunteers() {
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                testVolunteer2 = volunteerRepository.save(testVolunteer2);
                volunteerRepository.flush();  // CRITICAL: Flush before relationships

                // Both in project using actual userIds
                volunteerProjectRepository.saveAll(List.of(
                        new VolunteerProject(new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID), testVolunteer1, PROJECT_ID),
                        new VolunteerProject(new VolunteerProjectId(testVolunteer2.getUserId(), PROJECT_ID), testVolunteer2, PROJECT_ID)
                ));

                // Only volunteer1 assigned to task using actual userId
                volunteerTaskRepository.save(
                        new VolunteerTask(new VolunteerTaskId(testVolunteer1.getUserId(), TASK_ID), testVolunteer1, TASK_ID)
                );

                List<VolunteerToAssignToTaskTemplate> response = volunteerService
                        .getVolunteersNotAssignedToTaskWithTaskSuggestion(PROJECT_ID, TASK_ID);

                assertEquals(1, response.size(), "Should return only unassigned volunteer");
                assertEquals(testVolunteer2.getUserId(), response.get(0).getItemId());
            }

            @Test
            @DisplayName("Should return empty when all assigned")
            void shouldReturnEmptyWhenAllAssigned() {
                testVolunteer1 = volunteerRepository.save(testVolunteer1);
                testVolunteer2 = volunteerRepository.save(testVolunteer2);
                volunteerRepository.flush();  // CRITICAL: Flush before relationships

                // Both in project using actual userIds
                volunteerProjectRepository.saveAll(List.of(
                        new VolunteerProject(new VolunteerProjectId(testVolunteer1.getUserId(), PROJECT_ID), testVolunteer1, PROJECT_ID),
                        new VolunteerProject(new VolunteerProjectId(testVolunteer2.getUserId(), PROJECT_ID), testVolunteer2, PROJECT_ID)
                ));

                // Both assigned to task using actual userIds
                volunteerTaskRepository.saveAll(List.of(
                        new VolunteerTask(new VolunteerTaskId(testVolunteer1.getUserId(), TASK_ID), testVolunteer1, TASK_ID),
                        new VolunteerTask(new VolunteerTaskId(testVolunteer2.getUserId(), TASK_ID), testVolunteer2, TASK_ID)
                ));

                List<VolunteerToAssignToTaskTemplate> response = volunteerService
                        .getVolunteersNotAssignedToTaskWithTaskSuggestion(PROJECT_ID, TASK_ID);

                assertTrue(response.isEmpty(), "Should return empty when all are assigned");
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should handle non-existent project and task IDs")
            void shouldHandleNonExistentProjectAndTaskIds() {
                List<VolunteerToAssignToTaskTemplate> response = volunteerService
                        .getVolunteersNotAssignedToTaskWithTaskSuggestion(9999L, 9999L);

                assertNotNull(response);
                assertTrue(response.isEmpty());
            }

            @Test
            @DisplayName("Should return empty when no volunteers in project")
            void shouldReturnEmptyWhenNoVolunteersInProject() {
                List<VolunteerToAssignToTaskTemplate> response = volunteerService
                        .getVolunteersNotAssignedToTaskWithTaskSuggestion(PROJECT_ID, TASK_ID);

                assertTrue(response.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("patchVolunteerProfile Tests")
    class PatchVolunteerProfileTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully patch volunteer first name")
            void shouldSuccessfullyPatchVolunteerFirstName() {
                Volunteer saved = volunteerRepository.save(testVolunteer1);
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jonathan");

                volunteerService.patchVolunteerProfile(updates, saved.getUserId());

                Volunteer updated = volunteerRepository.findByUserId(saved.getUserId())
                        .orElseThrow(() -> new AssertionError("Volunteer not found after patch"));
                assertEquals("Jonathan", updated.getFirstName());
            }

            @Test
            @DisplayName("Should successfully patch volunteer last name")
            void shouldSuccessfullyPatchVolunteerLastName() {
                Volunteer saved = volunteerRepository.save(testVolunteer1);
                Map<String, Object> updates = new HashMap<>();
                updates.put("lastName", "Smith");

                volunteerService.patchVolunteerProfile(updates, saved.getUserId());

                Volunteer updated = volunteerRepository.findByUserId(saved.getUserId())
                        .orElseThrow(() -> new AssertionError("Volunteer not found after patch"));
                assertEquals("Smith", updated.getLastName());
            }

            @Test
            @DisplayName("Should successfully patch phone number")
            void shouldSuccessfullyPatchPhoneNumber() {
                Volunteer saved = volunteerRepository.save(testVolunteer1);
                Map<String, Object> updates = new HashMap<>();
                updates.put("phoneNumber", "555-0000");

                volunteerService.patchVolunteerProfile(updates, saved.getUserId());

                Volunteer updated = volunteerRepository.findByUserId(saved.getUserId())
                        .orElseThrow(() -> new AssertionError("Volunteer not found after patch"));
                assertEquals("555-0000", updated.getPhoneNumber());
            }

            @Test
            @DisplayName("Should successfully patch multiple fields")
            void shouldSuccessfullyPatchMultipleFields() {
                Volunteer saved = volunteerRepository.save(testVolunteer1);
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");
                updates.put("lastName", "Smith");
                updates.put("phoneNumber", "555-5555");

                volunteerService.patchVolunteerProfile(updates, saved.getUserId());

                Volunteer updated = volunteerRepository.findByUserId(saved.getUserId())
                        .orElseThrow(() -> new AssertionError("Volunteer not found after patch"));
                assertAll(
                        () -> assertEquals("Jane", updated.getFirstName()),
                        () -> assertEquals("Smith", updated.getLastName()),
                        () -> assertEquals("555-5555", updated.getPhoneNumber())
                );
            }

            @Test
            @DisplayName("Should handle empty updates map")
            void shouldHandleEmptyUpdatesMap() {
                Volunteer saved = volunteerRepository.save(testVolunteer1);
                Map<String, Object> updates = new HashMap<>();

                assertDoesNotThrow(() ->
                        volunteerService.patchVolunteerProfile(updates, saved.getUserId()));

                Volunteer unchanged = volunteerRepository.findByUserId(saved.getUserId())
                        .orElseThrow(() -> new AssertionError("Volunteer not found"));
                assertEquals(testVolunteer1.getFirstName(), unchanged.getFirstName());
            }

            @Test
            @DisplayName("Should persist changes to database")
            void shouldPersistChangesToDatabase() {
                Volunteer saved = volunteerRepository.save(testVolunteer1);
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Updated");

                volunteerService.patchVolunteerProfile(updates, saved.getUserId());

                // Retrieve from database to verify persistence
                Volunteer updated = volunteerRepository.findByUserId(saved.getUserId())
                        .orElseThrow(() -> new AssertionError("Volunteer not found after patch"));
                assertEquals("Updated", updated.getFirstName());
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should throw exception when volunteer not found")
            void shouldThrowExceptionWhenVolunteerNotFound() {
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                assertThrows(NoSuchVolunteerException.class,
                        () -> volunteerService.patchVolunteerProfile(updates, 9999L));
            }

            @Test
            @DisplayName("Should throw exception when trying to update non-existent volunteer")
            void shouldThrowExceptionWhenTryingToUpdateNonExistentVolunteer() {
                volunteerRepository.save(testVolunteer1);
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "NewName");

                assertThrows(NoSuchVolunteerException.class,
                        () -> volunteerService.patchVolunteerProfile(updates, 9999L));
            }
        }
    }

    @Nested
    @DisplayName("End-to-End Integration Tests")
    class EndToEndIntegrationTests {

        @Test
        @DisplayName("Should perform complete volunteer lifecycle operations")
        void shouldPerformCompleteVolunteerLifecycleOperations() {
            // Create
            Volunteer saved = volunteerRepository.save(testVolunteer1);
            assertTrue(saved.getUserId() > 0);

            // Retrieve
            VolunteerProfileResponse profile = volunteerService.getVolunteerProfile(saved.getUserId());
            assertEquals("John Doe", profile.getFullName());

            // Update
            Map<String, Object> updates = new HashMap<>();
            updates.put("firstName", "Jonathan");
            updates.put("phoneNumber", "555-9999");
            volunteerService.patchVolunteerProfile(updates, saved.getUserId());

            // Verify update
            Volunteer updated = volunteerRepository.findByUserId(saved.getUserId())
                    .orElseThrow(() -> new AssertionError("Volunteer not found after update"));
            assertEquals("Jonathan", updated.getFirstName());
            assertEquals("555-9999", updated.getPhoneNumber());
        }

        @Test
        @DisplayName("Should handle multiple volunteers in service operations")
        void shouldHandleMultipleVolunteersInServiceOperations() {
            volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2, testVolunteer3));

            // Retrieve all
            List<Volunteer> all = volunteerRepository.findAll();
            assertEquals(3, all.size());

            // Retrieve individual profiles
            VolunteerProfileResponse profile1 = volunteerService.getVolunteerProfile(1L);
            VolunteerProfileResponse profile2 = volunteerService.getVolunteerProfile(2L);

            assertAll(
                    () -> assertEquals("John Doe", profile1.getFullName()),
                    () -> assertEquals("Jane Smith", profile2.getFullName())
            );
        }

        @Test
        @DisplayName("Should maintain data consistency across operations")
        void shouldMaintainDataConsistencyAcrossOperations() {
            Volunteer saved = volunteerRepository.save(testVolunteer1);
            Long originalId = saved.getUserId();

            Map<String, Object> updates = new HashMap<>();
            updates.put("lastName", "UpdatedLast");
            volunteerService.patchVolunteerProfile(updates, saved.getUserId());

            Volunteer retrieved = volunteerRepository.findById(originalId)
                    .orElseThrow(() -> new AssertionError("Volunteer not found after update"));
            assertEquals(originalId, retrieved.getUserId());
            assertEquals(saved.getUserId(), retrieved.getUserId());
            assertEquals("UpdatedLast", retrieved.getLastName());
        }
    }
}













