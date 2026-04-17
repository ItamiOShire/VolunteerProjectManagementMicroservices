package com.vpm.volunteerserver.unit.service;

import com.vpm.volunteerserver.dto.response.VolunteerProfileResponse;
import com.vpm.volunteerserver.dto.template.VolunteerToAssignToTaskTemplate;
import com.vpm.volunteerserver.entity.Volunteer;
import com.vpm.volunteerserver.entity.VolunteerTask;
import com.vpm.volunteerserver.exception.volunteer.NoSuchVolunteerException;
import com.vpm.volunteerserver.repository.VolunteerRepository;
import com.vpm.volunteerserver.service.VolunteerService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeansException;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class VolunteerServiceTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @InjectMocks
    private VolunteerService volunteerService;

    /*
     * Test data
     */
    private Volunteer testVolunteer;
    private Long testVolunteerUserId;
    private VolunteerProfileResponse expectedResponse;

    @BeforeEach
    void setUp() {
        testVolunteerUserId = 1L;

        testVolunteer = Volunteer.builder()
                .userId(testVolunteerUserId)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .contactEmail("contact_test@example.com")
                .phoneNumber("555-1234")
                .build();

        expectedResponse = VolunteerProfileResponse.builder()
                .fullName("John Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .contact("contact_test@example.com / 555-1234")
                .build();
    }

    @Nested
    @DisplayName("getVolunteerProfile tests")
    class GetVolunteerProfileTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF GET PROFILE TEST ---");
                when(volunteerRepository.findByUserId(testVolunteerUserId))
                        .thenReturn(Optional.of(testVolunteer));
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF GET PROFILE TEST ---");
            }

            @Test
            @DisplayName("Should successfully retrieve volunteer profile")
            void shouldSuccessfullyRetrieveVolunteerProfile() {
                VolunteerProfileResponse response = volunteerService.getVolunteerProfile(testVolunteerUserId);

                assertNotNull(response, "Response should not be null");
                assertEquals(expectedResponse.getFullName(), response.getFullName(), "Full name should match");
                assertEquals(expectedResponse.getDateOfBirth(), response.getDateOfBirth(), "Date of birth should match");
                assertEquals(expectedResponse.getContact(), response.getContact(), "Contact should match");

                verify(volunteerRepository, times(1)).findByUserId(testVolunteerUserId);
            }

            @Test
            @DisplayName("Should retrieve volunteer profile with correct full name format")
            void shouldRetrieveWithCorrectFullNameFormat() {
                VolunteerProfileResponse response = volunteerService.getVolunteerProfile(testVolunteerUserId);

                assertTrue(response.getFullName().contains("John"), "Full name should contain first name");
                assertTrue(response.getFullName().contains("Doe"), "Full name should contain last name");
                assertEquals("John Doe", response.getFullName(), "Full name format should be 'FirstName LastName'");
            }

            @Test
            @DisplayName("Should retrieve correct date of birth")
            void shouldRetrieveCorrectDateOfBirth() {
                VolunteerProfileResponse response = volunteerService.getVolunteerProfile(testVolunteerUserId);

                assertEquals(LocalDate.of(1990, 5, 15), response.getDateOfBirth(), "Date of birth should match");
            }

            @Test
            @DisplayName("Should retrieve correct contact information")
            void shouldRetrieveCorrectContactInformation() {
                VolunteerProfileResponse response = volunteerService.getVolunteerProfile(testVolunteerUserId);

                assertEquals("contact_test@example.com / 555-1234", response.getContact(), "Contact information should match phone number");
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF NEGATIVE GET PROFILE TEST ---");
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF NEGATIVE GET PROFILE TEST ---");
            }

            @Test
            @DisplayName("Should throw NoSuchVolunteerException when volunteer not found")
            void shouldThrowNoSuchVolunteerException() {
                when(volunteerRepository.findByUserId(testVolunteerUserId))
                        .thenReturn(Optional.empty());

                assertThrows(
                        NoSuchVolunteerException.class,
                        () -> volunteerService.getVolunteerProfile(testVolunteerUserId),
                        "Should throw NoSuchVolunteerException when volunteer not found"
                );

                verify(volunteerRepository, times(1)).findByUserId(testVolunteerUserId);
            }

            @Test
            @DisplayName("Should call repository with correct userId")
            void shouldCallRepositoryWithCorrectUserId() {
                when(volunteerRepository.findByUserId(testVolunteerUserId))
                        .thenReturn(Optional.empty());

                try {
                    volunteerService.getVolunteerProfile(testVolunteerUserId);
                } catch (NoSuchVolunteerException e) {
                    // Expected
                }

                verify(volunteerRepository, times(1)).findByUserId(testVolunteerUserId);
            }

            @Test
            @DisplayName("Should throw exception for non-existent user ID")
            void shouldThrowExceptionForNonExistentUserId() {
                Long nonExistentUserId = 999L;
                when(volunteerRepository.findByUserId(nonExistentUserId))
                        .thenReturn(Optional.empty());

                assertThrows(
                        NoSuchVolunteerException.class,
                        () -> volunteerService.getVolunteerProfile(nonExistentUserId)
                );

                verify(volunteerRepository, times(1)).findByUserId(nonExistentUserId);
            }
        }
    }

    @Nested
    @DisplayName("patchVolunteerProfile tests")
    class PatchVolunteerProfileTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF PATCH PROFILE TEST ---");
                when(volunteerRepository.findByUserId(testVolunteerUserId))
                        .thenReturn(Optional.of(testVolunteer));
                when(volunteerRepository.save(any(Volunteer.class)))
                        .thenReturn(testVolunteer);
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF PATCH PROFILE TEST ---");
            }

            @Test
            @DisplayName("Should successfully patch volunteer profile")
            void shouldSuccessfullyPatchVolunteerProfile() {
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                assertDoesNotThrow(() ->
                    volunteerService.patchVolunteerProfile(updates, testVolunteerUserId),
                    "Should not throw exception when patching valid profile"
                );

                verify(volunteerRepository, times(1)).findByUserId(testVolunteerUserId);
                verify(volunteerRepository, times(1)).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should update phone number")
            void shouldUpdatePhoneNumber() {
                Map<String, Object> updates = new HashMap<>();
                updates.put("phoneNumber", "555-9999");

                volunteerService.patchVolunteerProfile(updates, testVolunteerUserId);

                verify(volunteerRepository, times(1)).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should update multiple fields")
            void shouldUpdateMultipleFields() {
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");
                updates.put("lastName", "Smith");
                updates.put("phoneNumber", "555-5555");

                volunteerService.patchVolunteerProfile(updates, testVolunteerUserId);

                verify(volunteerRepository, times(1)).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should patch with empty updates map")
            void shouldPatchWithEmptyUpdatesMap() {
                Map<String, Object> updates = new HashMap<>();

                assertDoesNotThrow(() ->
                    volunteerService.patchVolunteerProfile(updates, testVolunteerUserId)
                );

                verify(volunteerRepository, times(1)).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should save volunteer after patching")
            void shouldSaveVolunteerAfterPatching() {
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "UpdatedName");

                volunteerService.patchVolunteerProfile(updates, testVolunteerUserId);

                verify(volunteerRepository, times(1)).save(testVolunteer);
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF NEGATIVE PATCH PROFILE TEST ---");
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF NEGATIVE PATCH PROFILE TEST ---");
            }

            @Test
            @DisplayName("Should throw NoSuchVolunteerException when volunteer not found")
            void shouldThrowNoSuchVolunteerExceptionWhenNotFound() {
                when(volunteerRepository.findByUserId(testVolunteerUserId))
                        .thenReturn(Optional.empty());

                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                assertThrows(
                        NoSuchVolunteerException.class,
                        () -> volunteerService.patchVolunteerProfile(updates, testVolunteerUserId),
                        "Should throw NoSuchVolunteerException when volunteer not found"
                );

                verify(volunteerRepository, times(1)).findByUserId(testVolunteerUserId);
                verify(volunteerRepository, never()).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should throw exception for non-existent volunteer ID")
            void shouldThrowExceptionForNonExistentVolunteerId() {
                Long nonExistentUserId = 999L;
                when(volunteerRepository.findByUserId(nonExistentUserId))
                        .thenReturn(Optional.empty());

                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                assertThrows(
                        NoSuchVolunteerException.class,
                        () -> volunteerService.patchVolunteerProfile(updates, nonExistentUserId)
                );

                verify(volunteerRepository, never()).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should not save when volunteer not found")
            void shouldNotSaveWhenVolunteerNotFound() {
                when(volunteerRepository.findByUserId(testVolunteerUserId))
                        .thenReturn(Optional.empty());

                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                try {
                    volunteerService.patchVolunteerProfile(updates, testVolunteerUserId);
                } catch (NoSuchVolunteerException e) {
                    // Expected
                }

                verify(volunteerRepository, never()).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should throw BeansException on invalid property")
            void shouldThrowBeansExceptionOnInvalidProperty() {
                when(volunteerRepository.findByUserId(testVolunteerUserId))
                        .thenReturn(Optional.of(testVolunteer));

                Map<String, Object> updates = new HashMap<>();
                updates.put("invalidProperty", "someValue");

                assertThrows(
                        BeansException.class,
                        () -> volunteerService.patchVolunteerProfile(updates, testVolunteerUserId),
                        "Should throw BeansException for invalid property"
                );
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProject tests")
    class GetVolunteersInProjectTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF GET VOLUNTEERS IN PROJECT TEST ---");
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF GET VOLUNTEERS IN PROJECT TEST ---");
            }

            @Test
            @DisplayName("Should successfully retrieve volunteers in project")
            void shouldSuccessfullyRetrieveVolunteersInProject() {
                Long projectId = 1L;
                List<Volunteer> volunteers = new ArrayList<>();
                volunteers.add(testVolunteer);

                when(volunteerRepository.getVolunteersInProjectByProjectId(projectId))
                        .thenReturn(volunteers);

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProject(projectId);

                assertNotNull(response, "Response should not be null");
                assertEquals(1, response.size(), "Should return one volunteer");
                assertEquals("John Doe", response.get(0).getFullName(), "Full name should match");

                verify(volunteerRepository, times(1)).getVolunteersInProjectByProjectId(projectId);
            }

            @Test
            @DisplayName("Should retrieve multiple volunteers in project")
            void shouldRetrieveMultipleVolunteersInProject() {
                Long projectId = 1L;
                Volunteer volunteer2 = Volunteer.builder()
                        .userId(2L)
                        .firstName("Jane")
                        .lastName("Smith")
                        .dateOfBirth(LocalDate.of(1995, 3, 20))
                        .phoneNumber("555-9999")
                        .build();

                List<Volunteer> volunteers = new ArrayList<>();
                volunteers.add(testVolunteer);
                volunteers.add(volunteer2);

                when(volunteerRepository.getVolunteersInProjectByProjectId(projectId))
                        .thenReturn(volunteers);

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProject(projectId);

                assertEquals(2, response.size(), "Should return two volunteers");
                assertEquals("John Doe", response.get(0).getFullName());
                assertEquals("Jane Smith", response.get(1).getFullName());

                verify(volunteerRepository, times(1)).getVolunteersInProjectByProjectId(projectId);
            }

            @Test
            @DisplayName("Should retrieve empty list when no volunteers in project")
            void shouldRetrieveEmptyListWhenNoVolunteersInProject() {
                Long projectId = 1L;

                when(volunteerRepository.getVolunteersInProjectByProjectId(projectId))
                        .thenReturn(new ArrayList<>());

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProject(projectId);

                assertNotNull(response, "Response should not be null");
                assertTrue(response.isEmpty(), "List should be empty");

                verify(volunteerRepository, times(1)).getVolunteersInProjectByProjectId(projectId);
            }

            @Test
            @DisplayName("Should map volunteers correctly to profile response")
            void shouldMapVolunteersCorrectlyToProfileResponse() {
                Long projectId = 1L;
                List<Volunteer> volunteers = new ArrayList<>();
                volunteers.add(testVolunteer);

                when(volunteerRepository.getVolunteersInProjectByProjectId(projectId))
                        .thenReturn(volunteers);

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProject(projectId);

                assertEquals(expectedResponse.getFullName(), response.get(0).getFullName());
                assertEquals(expectedResponse.getDateOfBirth(), response.get(0).getDateOfBirth());
                assertEquals(expectedResponse.getContact(), response.get(0).getContact());

                verify(volunteerRepository, times(1)).getVolunteersInProjectByProjectId(projectId);
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF NEGATIVE GET VOLUNTEERS IN PROJECT TEST ---");
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF NEGATIVE GET VOLUNTEERS IN PROJECT TEST ---");
            }

            @Test
            @DisplayName("Should handle repository throwing exception")
            void shouldHandleRepositoryThrowingException() {
                Long projectId = 1L;

                when(volunteerRepository.getVolunteersInProjectByProjectId(projectId))
                        .thenThrow(new RuntimeException("Database error"));

                assertThrows(RuntimeException.class,
                        () -> volunteerService.getVolunteersInProject(projectId),
                        "Should throw exception when repository fails");

                verify(volunteerRepository, times(1)).getVolunteersInProjectByProjectId(projectId);
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProjectWhoAreAssignedToTask tests")
    class GetVolunteersAssignedToTaskTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF GET VOLUNTEERS ASSIGNED TO TASK TEST ---");
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF GET VOLUNTEERS ASSIGNED TO TASK TEST ---");
            }

            @Test
            @DisplayName("Should successfully retrieve assigned volunteers")
            void shouldSuccessfullyRetrieveAssignedVolunteers() {
                Long projectId = 1L;
                Long taskId = 1L;
                List<Volunteer> volunteers = new ArrayList<>();
                volunteers.add(testVolunteer);

                when(volunteerRepository.getVolunteersInProjectAssignedToTask(projectId, taskId))
                        .thenReturn(volunteers);

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProjectWhoAreAssignedToTask(projectId, taskId);

                assertNotNull(response, "Response should not be null");
                assertEquals(1, response.size(), "Should return one volunteer");
                assertEquals("John Doe", response.get(0).getFullName());

                verify(volunteerRepository, times(1)).getVolunteersInProjectAssignedToTask(projectId, taskId);
            }

            @Test
            @DisplayName("Should retrieve empty list when no assigned volunteers")
            void shouldRetrieveEmptyListWhenNoAssignedVolunteers() {
                Long projectId = 1L;
                Long taskId = 1L;

                when(volunteerRepository.getVolunteersInProjectAssignedToTask(projectId, taskId))
                        .thenReturn(new ArrayList<>());

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProjectWhoAreAssignedToTask(projectId, taskId);

                assertTrue(response.isEmpty(), "List should be empty");

                verify(volunteerRepository, times(1)).getVolunteersInProjectAssignedToTask(projectId, taskId);
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF NEGATIVE GET VOLUNTEERS ASSIGNED TO TASK TEST ---");
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF NEGATIVE GET VOLUNTEERS ASSIGNED TO TASK TEST ---");
            }

            @Test
            @DisplayName("Should handle repository throwing exception")
            void shouldHandleRepositoryThrowingException() {
                Long projectId = 1L;
                Long taskId = 1L;

                when(volunteerRepository.getVolunteersInProjectAssignedToTask(projectId, taskId))
                        .thenThrow(new RuntimeException("Database error"));

                assertThrows(RuntimeException.class,
                        () -> volunteerService.getVolunteersInProjectWhoAreAssignedToTask(projectId, taskId));

                verify(volunteerRepository, times(1)).getVolunteersInProjectAssignedToTask(projectId, taskId);
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProjectWhoAreNotInTask tests")
    class GetVolunteersNotInTaskTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF GET VOLUNTEERS NOT IN TASK TEST ---");
                when(volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(anyLong(), anyLong()))
                        .thenReturn(new ArrayList<>());
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF GET VOLUNTEERS NOT IN TASK TEST ---");
            }

            @Test
            @DisplayName("Should successfully retrieve volunteers not in task")
            void shouldSuccessfullyRetrieveVolunteersNotInTask() {
                Long projectId = 1L;
                Long taskId = 1L;

                Volunteer volunteerNotAssigned = Volunteer.builder()
                        .userId(2L)
                        .firstName("Jane")
                        .lastName("Smith")
                        .dateOfBirth(LocalDate.of(1995, 3, 20))
                        .phoneNumber("555-9999")
                        .volunteerTasks(new ArrayList<>())
                        .build();

                List<Volunteer> volunteers = new ArrayList<>();
                volunteers.add(volunteerNotAssigned);

                when(volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId))
                        .thenReturn(volunteers);

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProjectWhoAreNotInTask(projectId, taskId);

                assertEquals(1, response.size(), "Should return one volunteer");
                assertEquals("Jane Smith", response.get(0).getFullName());

                verify(volunteerRepository, times(1)).getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId);
            }

            @Test
            @DisplayName("Should filter out assigned volunteers")
            void shouldFilterOutAssignedVolunteers() {
                Long projectId = 1L;
                Long taskId = 1L;

                VolunteerTask task = new VolunteerTask();
                Volunteer assignedVolunteer = Volunteer.builder()
                        .userId(2L)
                        .firstName("Jane")
                        .lastName("Smith")
                        .dateOfBirth(LocalDate.of(1995, 3, 20))
                        .phoneNumber("555-9999")
                        .volunteerTasks(new ArrayList<>(Collections.singletonList(task)))
                        .build();

                List<Volunteer> volunteers = new ArrayList<>();
                volunteers.add(assignedVolunteer);

                when(volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId))
                        .thenReturn(volunteers);

                List<VolunteerProfileResponse> response = volunteerService.getVolunteersInProjectWhoAreNotInTask(projectId, taskId);

                assertTrue(response.isEmpty(), "Should not return assigned volunteers");

                verify(volunteerRepository, times(1)).getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId);
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersNotAssignedToTaskWithTaskSuggestion tests")
    class GetVolunteersNotAssignedWithTaskSuggestionTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF GET UNASSIGNED VOLUNTEERS WITH TASK SUGGESTION TEST ---");
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF GET UNASSIGNED VOLUNTEERS WITH TASK SUGGESTION TEST ---");
            }

            @Test
            @DisplayName("Should return unassigned volunteers with task suggestion flag")
            void shouldReturnUnassignedVolunteersWithTaskSuggestionFlag() {
                Long projectId = 1L;
                Long taskId = 1L;

                Volunteer volunteer1 = Volunteer.builder()
                        .userId(1L)
                        .firstName("John")
                        .lastName("Doe")
                        .dateOfBirth(LocalDate.of(1990, 5, 15))
                        .phoneNumber("555-1234")
                        .volunteerTasks(new ArrayList<>())
                        .build();

                Volunteer volunteer2 = Volunteer.builder()
                        .userId(2L)
                        .firstName("Jane")
                        .lastName("Smith")
                        .dateOfBirth(LocalDate.of(1995, 3, 20))
                        .phoneNumber("555-9999")
                        .volunteerTasks(new ArrayList<>())
                        .build();

                List<Volunteer> allVolunteers = new ArrayList<>();
                allVolunteers.add(volunteer1);
                allVolunteers.add(volunteer2);

                List<Volunteer> whoReportedSuggestion = new ArrayList<>();
                whoReportedSuggestion.add(volunteer1);

                when(volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId))
                        .thenReturn(allVolunteers);
                when(volunteerRepository.getVolunteersInProjectWhoReportedTaskSuggestion(projectId, taskId))
                        .thenReturn(whoReportedSuggestion);

                List<VolunteerToAssignToTaskTemplate> response = volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(projectId, taskId);

                assertEquals(2, response.size(), "Should return two volunteers");
                assertTrue(response.get(0).isReportedTaskSuggestion(), "First volunteer should have reported task suggestion");
                assertFalse(response.get(1).isReportedTaskSuggestion(), "Second volunteer should not have reported task suggestion");

                verify(volunteerRepository, times(1)).getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId);
                verify(volunteerRepository, times(1)).getVolunteersInProjectWhoReportedTaskSuggestion(projectId, taskId);
            }

            @Test
            @DisplayName("Should return empty list when no unassigned volunteers")
            void shouldReturnEmptyListWhenNoUnassignedVolunteers() {
                Long projectId = 1L;
                Long taskId = 1L;

                when(volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId))
                        .thenReturn(new ArrayList<>());
                when(volunteerRepository.getVolunteersInProjectWhoReportedTaskSuggestion(projectId, taskId))
                        .thenReturn(new ArrayList<>());

                List<VolunteerToAssignToTaskTemplate> response = volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(projectId, taskId);

                assertTrue(response.isEmpty(), "Should return empty list");

                verify(volunteerRepository, times(1)).getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId);
            }

            @Test
            @DisplayName("Should correctly map volunteer to template")
            void shouldCorrectlyMapVolunteerToTemplate() {
                Long projectId = 1L;
                Long taskId = 1L;

                Volunteer volunteer = Volunteer.builder()
                        .userId(1L)
                        .firstName("John")
                        .lastName("Doe")
                        .dateOfBirth(LocalDate.of(1990, 5, 15))
                        .phoneNumber("555-1234")
                        .volunteerTasks(new ArrayList<>())
                        .build();

                List<Volunteer> allVolunteers = new ArrayList<>();
                allVolunteers.add(volunteer);

                when(volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId))
                        .thenReturn(allVolunteers);
                when(volunteerRepository.getVolunteersInProjectWhoReportedTaskSuggestion(projectId, taskId))
                        .thenReturn(allVolunteers);

                List<VolunteerToAssignToTaskTemplate> response = volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(projectId, taskId);

                assertEquals(1, response.size());
                assertEquals(1L, response.get(0).getItemId(), "itemId should match userId");
                assertEquals("John Doe", response.get(0).getFullName(), "Full name should be correctly formatted");
                assertTrue(response.get(0).isReportedTaskSuggestion());
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF NEGATIVE GET UNASSIGNED VOLUNTEERS WITH TASK SUGGESTION TEST ---");
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF NEGATIVE GET UNASSIGNED VOLUNTEERS WITH TASK SUGGESTION TEST ---");
            }

            @Test
            @DisplayName("Should handle repository throwing exception")
            void shouldHandleRepositoryThrowingException() {
                Long projectId = 1L;
                Long taskId = 1L;

                when(volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId))
                        .thenThrow(new RuntimeException("Database error"));

                assertThrows(RuntimeException.class,
                        () -> volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(projectId, taskId));

                verify(volunteerRepository, times(1)).getAllVolunteersInProjectAssignedOrNotToTask(projectId, taskId);
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProjectWhoReportedTaskSuggestion tests")
    class GetVolunteersWithTaskSuggestionTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF GET VOLUNTEERS WITH TASK SUGGESTION TEST ---");
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF GET VOLUNTEERS WITH TASK SUGGESTION TEST ---");
            }

            @Test
            @DisplayName("Should successfully retrieve volunteers who reported task suggestion")
            void shouldSuccessfullyRetrieveVolunteersWithTaskSuggestion() {
                Long projectId = 1L;
                Long taskId = 1L;
                List<Volunteer> volunteers = new ArrayList<>();
                volunteers.add(testVolunteer);

                when(volunteerRepository.getVolunteersInProjectWhoReportedTaskSuggestion(projectId, taskId))
                        .thenReturn(volunteers);

                List<VolunteerToAssignToTaskTemplate> response = volunteerService.getVolunteersInProjectWhoReportedTaskSuggestion(projectId, taskId);

                assertEquals(1, response.size(), "Should return one volunteer");
                assertEquals(testVolunteer.getUserId(), response.get(0).getItemId());

                verify(volunteerRepository, times(1)).getVolunteersInProjectWhoReportedTaskSuggestion(projectId, taskId);
            }

            @Test
            @DisplayName("Should retrieve empty list when no volunteers reported suggestion")
            void shouldRetrieveEmptyListWhenNoVolunteersReportedSuggestion() {
                Long projectId = 1L;
                Long taskId = 1L;

                when(volunteerRepository.getVolunteersInProjectWhoReportedTaskSuggestion(projectId, taskId))
                        .thenReturn(new ArrayList<>());

                List<VolunteerToAssignToTaskTemplate> response = volunteerService.getVolunteersInProjectWhoReportedTaskSuggestion(projectId, taskId);

                assertTrue(response.isEmpty(), "List should be empty");

                verify(volunteerRepository, times(1)).getVolunteersInProjectWhoReportedTaskSuggestion(projectId, taskId);
            }
        }
    }
}


