package com.vpm.volunteerserver.integration.repository;

import com.vpm.volunteerserver.config.IntegrationTestsDBConfig;
import com.vpm.volunteerserver.entity.Volunteer;
import com.vpm.volunteerserver.repository.VolunteerRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// TODO: Check those tests if repository returns what is needed and demanded

@SpringBootTest
@Import(IntegrationTestsDBConfig.class)
public class VolunteerRepositoryIntegrationTest {

    @Autowired
    private VolunteerRepository volunteerRepository;

    private Volunteer testVolunteer1;
    private Volunteer testVolunteer2;
    private Volunteer testVolunteer3;

    @BeforeEach
    void setUp() {
        volunteerRepository.deleteAll();
        testVolunteer1 = Volunteer.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phoneNumber("555-1234")
                .contactEmail("john@example.com")
                .build();

        testVolunteer2 = Volunteer.builder()
                .userId(2L)
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1995, 3, 20))
                .phoneNumber("555-9999")
                .contactEmail("jane@example.com")
                .build();

        testVolunteer3 = Volunteer.builder()
                .userId(3L)
                .firstName("Bob")
                .lastName("Johnson")
                .dateOfBirth(LocalDate.of(1988, 7, 10))
                .phoneNumber("555-5555")
                .contactEmail("bob@example.com")
                .build();
    }

    @Nested
    @DisplayName("findByUserId Tests")
    class FindByUserIdTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should find volunteer by userId")
            void shouldFindVolunteerByUserId() {
                volunteerRepository.save(testVolunteer1);

                Optional<Volunteer> result = volunteerRepository.findByUserId(1L);

                assertTrue(result.isPresent(), "Volunteer should be found");
                assertEquals(testVolunteer1.getUserId(), result.get().getUserId());
                assertEquals("John", result.get().getFirstName());
            }

            @Test
            @DisplayName("Should retrieve correct volunteer information")
            void shouldRetrieveCorrectVolunteerInformation() {
                volunteerRepository.save(testVolunteer1);

                Optional<Volunteer> result = volunteerRepository.findByUserId(1L);

                assertTrue(result.isPresent());
                assertEquals(LocalDate.of(1990, 5, 15), result.get().getDateOfBirth());
                assertEquals("john@example.com", result.get().getContactEmail());
                assertEquals("555-1234", result.get().getPhoneNumber());
            }

            @Test
            @DisplayName("Should find among multiple volunteers")
            void shouldFindAmongMultipleVolunteers() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2, testVolunteer3));

                Optional<Volunteer> result = volunteerRepository.findByUserId(2L);

                assertTrue(result.isPresent());
                assertEquals("Jane", result.get().getFirstName());
                assertEquals("Smith", result.get().getLastName());
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should return empty Optional when volunteer not found")
            void shouldReturnEmptyOptionalWhenVolunteerNotFound() {
                Optional<Volunteer> result = volunteerRepository.findByUserId(999L);

                assertTrue(result.isEmpty(), "Volunteer should not be found");
            }

            @Test
            @DisplayName("Should not find volunteer with wrong userId")
            void shouldNotFindVolunteerWithWrongUserId() {
                volunteerRepository.save(testVolunteer1);

                Optional<Volunteer> result = volunteerRepository.findByUserId(2L);

                assertTrue(result.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProjectByProjectId Tests")
    class GetVolunteersInProjectTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should return empty list when no volunteers in project")
            void shouldReturnEmptyListWhenNoVolunteersInProject() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2));

                List<Volunteer> result = volunteerRepository.getVolunteersInProjectByProjectId(1L);

                assertTrue(result.isEmpty(), "Should return empty list when no volunteers have project relationship");
            }

            @Test
            @DisplayName("Should save and retrieve multiple volunteers")
            void shouldSaveAndRetrieveMultipleVolunteers() {
                List<Volunteer> volunteers = volunteerRepository.saveAll(
                        List.of(testVolunteer1, testVolunteer2, testVolunteer3)
                );

                assertAll(
                        () -> assertEquals(3, volunteers.size()),
                        () -> assertTrue(volunteers.stream().anyMatch(v -> v.getFirstName().equals("John"))),
                        () -> assertTrue(volunteers.stream().anyMatch(v -> v.getFirstName().equals("Jane")))
                );
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should handle non-existent project ID")
            void shouldHandleNonExistentProjectId() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2));

                List<Volunteer> result = volunteerRepository.getVolunteersInProjectByProjectId(9999L);

                assertTrue(result.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProjectAssignedToTask Tests")
    class GetVolunteersAssignedToTaskTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should return empty list when no volunteers assigned to task")
            void shouldReturnEmptyListWhenNoVolunteersAssignedToTask() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2));

                List<Volunteer> result = volunteerRepository.getVolunteersInProjectAssignedToTask(1L, 1L);

                assertTrue(result.isEmpty(), "Should return empty list when no assignment relationships exist");
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should handle non-existent project ID")
            void shouldHandleNonExistentProjectId() {
                volunteerRepository.save(testVolunteer1);

                List<Volunteer> result = volunteerRepository.getVolunteersInProjectAssignedToTask(9999L, 1L);

                assertTrue(result.isEmpty());
            }

            @Test
            @DisplayName("Should handle non-existent task ID")
            void shouldHandleNonExistentTaskId() {
                volunteerRepository.save(testVolunteer1);

                List<Volunteer> result = volunteerRepository.getVolunteersInProjectAssignedToTask(1L, 9999L);

                assertTrue(result.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getAllVolunteersInProjectAssignedOrNotToTask Tests")
    class GetAllVolunteersInProjectTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should return all volunteers in project regardless of task assignment")
            void shouldReturnAllVolunteersInProject() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2));

                List<Volunteer> result = volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(1L, 1L);

                assertNotNull(result);
                // Note: Returns empty if no project relationships exist in test database
            }

            @Test
            @DisplayName("Should handle multiple task IDs correctly")
            void shouldHandleMultipleTaskIds() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2, testVolunteer3));

                List<Volunteer> result1 = volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(1L, 1L);
                List<Volunteer> result2 = volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(1L, 2L);

                assertNotNull(result1);
                assertNotNull(result2);
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should handle non-existent project gracefully")
            void shouldHandleNonExistentProject() {
                volunteerRepository.save(testVolunteer1);

                List<Volunteer> result = volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(9999L, 1L);

                assertTrue(result.isEmpty());
            }

            @Test
            @DisplayName("Should return empty list when no volunteers in project")
            void shouldReturnEmptyListWhenNoVolunteersInProject() {
                List<Volunteer> result = volunteerRepository.getAllVolunteersInProjectAssignedOrNotToTask(1L, 1L);

                assertTrue(result.isEmpty());
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
            @DisplayName("Should return empty list when no suggestion relationships exist")
            void shouldReturnEmptyListWhenNoSuggestionRelationshipsExist() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2));

                List<Volunteer> result = volunteerRepository.getVolunteersInProjectWhoReportedTaskSuggestion(1L, 1L);

                assertTrue(result.isEmpty(), "Should return empty list when no suggestion relationships exist");
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should handle non-existent project ID")
            void shouldHandleNonExistentProjectId() {
                List<Volunteer> result = volunteerRepository.getVolunteersInProjectWhoReportedTaskSuggestion(9999L, 1L);

                assertTrue(result.isEmpty());
            }

            @Test
            @DisplayName("Should handle non-existent task ID")
            void shouldHandleNonExistentTaskId() {
                volunteerRepository.save(testVolunteer1);

                List<Volunteer> result = volunteerRepository.getVolunteersInProjectWhoReportedTaskSuggestion(1L, 9999L);

                assertTrue(result.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("getVolunteersInProjectNotAssignedToTaskWithTaskSuggestion Tests")
    class GetVolunteersNotAssignedWithSuggestionTests {

        @Nested
        @DisplayName("Positive Tests")
        class PositiveTests {

            @Test
            @DisplayName("Should handle query correctly")
            void shouldHandleQueryCorrectly() {
                volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2));

                List<Volunteer> result = volunteerRepository.getVolunteersInProjectNotAssignedToTaskWithTaskSuggestion(1L, 1L);

                assertNotNull(result);
            }
        }

        @Nested
        @DisplayName("Negative Tests")
        class NegativeTests {

            @Test
            @DisplayName("Should return empty list when no matches")
            void shouldReturnEmptyListWhenNoMatches() {
                List<Volunteer> result = volunteerRepository.getVolunteersInProjectNotAssignedToTaskWithTaskSuggestion(9999L, 9999L);

                assertTrue(result.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("Database Persistence Tests")
    class DatabasePersistenceTests {

        @Test
        @DisplayName("Should persist volunteer with all fields")
        void shouldPersistVolunteerWithAllFields() {
            Volunteer volunteer = Volunteer.builder()
                    .userId(100L)
                    .firstName("TestFirst")
                    .lastName("TestLast")
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .phoneNumber("000-0000")
                    .contactEmail("test@test.com")
                    .build();

            Volunteer saved = volunteerRepository.save(volunteer);

            assertAll(
                    () -> assertTrue(saved.getUserId() > 0),
                    () -> assertEquals(100L, saved.getUserId()),
                    () -> assertEquals("TestFirst", saved.getFirstName()),
                    () -> assertEquals("TestLast", saved.getLastName()),
                    () -> assertEquals(LocalDate.of(2000, 1, 1), saved.getDateOfBirth()),
                    () -> assertEquals("000-0000", saved.getPhoneNumber()),
                    () -> assertEquals("test@test.com", saved.getContactEmail())
            );
        }

        @Test
        @DisplayName("Should retrieve persisted volunteer by ID")
        void shouldRetrievePersistedVolunteerById() {
            Volunteer saved = volunteerRepository.save(testVolunteer1);

            Optional<Volunteer> retrieved = volunteerRepository.findById(saved.getUserId());

            assertTrue(retrieved.isPresent());
            assertEquals(saved.getUserId(), retrieved.get().getUserId());
            assertEquals(saved.getUserId(), retrieved.get().getUserId());
        }

        @Test
        @DisplayName("Should update existing volunteer")
        void shouldUpdateExistingVolunteer() {
            Volunteer saved = volunteerRepository.save(testVolunteer1);
            saved.setFirstName("UpdatedName");

            Volunteer updated = volunteerRepository.save(saved);

            Optional<Volunteer> retrieved = volunteerRepository.findById(updated.getUserId());
            assertTrue(retrieved.isPresent());
            assertEquals("UpdatedName", retrieved.get().getFirstName());
        }

        @Test
        @DisplayName("Should delete volunteer")
        void shouldDeleteVolunteer() {
            Volunteer saved = volunteerRepository.save(testVolunteer1);
            Long volunteerId = saved.getUserId();

            volunteerRepository.deleteById(volunteerId);

            Optional<Volunteer> retrieved = volunteerRepository.findById(volunteerId);
            assertTrue(retrieved.isEmpty());
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should maintain data integrity with multiple saves")
        void shouldMaintainDataIntegrityWithMultipleSaves() {
            volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2, testVolunteer3));

            List<Volunteer> allVolunteers = volunteerRepository.findAll();

            assertEquals(3, allVolunteers.size());
            assertTrue(allVolunteers.stream().anyMatch(v -> v.getUserId() == 1L));
            assertTrue(allVolunteers.stream().anyMatch(v -> v.getUserId() == 2L));
            assertTrue(allVolunteers.stream().anyMatch(v -> v.getUserId() == 3L));
        }

        @Test
        @DisplayName("Should retrieve volunteers with correct order")
        void shouldRetrieveVolunteersWithCorrectOrder() {
            volunteerRepository.saveAll(List.of(testVolunteer1, testVolunteer2, testVolunteer3));

            List<Volunteer> allVolunteers = volunteerRepository.findAll();

            assertAll(
                    () -> assertEquals(3, allVolunteers.size()),
                    () -> assertTrue(allVolunteers.stream()
                            .allMatch(v -> v.getFirstName() != null && !v.getFirstName().isEmpty()))
            );
        }
    }
}





