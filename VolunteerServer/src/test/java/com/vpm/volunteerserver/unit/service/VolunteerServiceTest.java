package com.vpm.volunteerserver.unit.service;

import com.vpm.volunteerserver.dto.response.VolunteerProfileResponse;
import com.vpm.volunteerserver.entity.Volunteer;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
                .id(1L)
                .userId(testVolunteerUserId)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phoneNumber("555-1234")
                .build();

        expectedResponse = VolunteerProfileResponse.builder()
                .fullName("John Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .contact("555-1234")
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

                assertEquals("555-1234", response.getContact(), "Contact information should match phone number");
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
}


