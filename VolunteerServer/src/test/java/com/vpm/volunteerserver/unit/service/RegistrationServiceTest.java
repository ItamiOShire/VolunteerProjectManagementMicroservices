package com.vpm.volunteerserver.unit.service;

import com.vpm.volunteerserver.api.internal.AuthClient;
import com.vpm.volunteerserver.dto.request.VolunteerRegisterRequest;
import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import com.vpm.volunteerserver.entity.Volunteer;
import com.vpm.volunteerserver.exception.volunteer.VolunteerAlreadyExistsException;
import com.vpm.volunteerserver.repository.VolunteerRepository;
import com.vpm.volunteerserver.service.RegistrationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)

// Silents 'unnecessary stubbing exception'
@MockitoSettings(strictness = Strictness.LENIENT)
public class RegistrationServiceTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private RegistrationService registrationService;

    /*
     * Test data
     */
    private VolunteerRegisterRequest validRegistrationRequest;
    private AuthRegistrationResponse authRegistrationResponse;
    private Volunteer savedVolunteer;

    @BeforeEach
    void setUp() {
        validRegistrationRequest = new VolunteerRegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                LocalDate.of(1990, 5, 15),
                "555-1234"
        );

        authRegistrationResponse = new AuthRegistrationResponse();
        authRegistrationResponse.setUserId(1L);

        savedVolunteer = Volunteer.builder()
                .id(1L)
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phoneNumber("555-1234")
                .build();
    }

    @Nested
    @DisplayName("registerVolunteer tests")
    class RegisterVolunteerTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF REGISTRATION TEST ---");
                when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                        .thenReturn(authRegistrationResponse);
                when(volunteerRepository.save(any(Volunteer.class)))
                        .thenReturn(savedVolunteer);
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF REGISTRATION TEST ---");
            }

            @Test
            @DisplayName("Should successfully register a volunteer")
            void shouldSuccessfullyRegisterVolunteer() {
                assertDoesNotThrow(() ->
                    registrationService.registerVolunteer(validRegistrationRequest),
                    "Registration should not throw exception"
                );

                verify(authClient, times(1)).registerVolunteerInAuthService(any(AuthRegistrationRequest.class));
                verify(volunteerRepository, times(1)).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should call auth client with correct parameters")
            void shouldCallAuthClientWithCorrectParameters() {
                registrationService.registerVolunteer(validRegistrationRequest);

                ArgumentCaptor<AuthRegistrationRequest> captor = ArgumentCaptor.forClass(AuthRegistrationRequest.class);
                verify(authClient).registerVolunteerInAuthService(captor.capture());

                AuthRegistrationRequest capturedRequest = captor.getValue();
                assertEquals("john.doe@example.com", capturedRequest.getEmail(), "Email should match");
                assertEquals("password123", capturedRequest.getPassword(), "Password should match");
                assertEquals("VOLUNTEER", capturedRequest.getRole(), "Role should be VOLUNTEER");
            }

            @Test
            @DisplayName("Should save volunteer with correct data from auth response")
            void shouldSaveVolunteerWithCorrectData() {
                registrationService.registerVolunteer(validRegistrationRequest);

                ArgumentCaptor<Volunteer> captor = ArgumentCaptor.forClass(Volunteer.class);
                verify(volunteerRepository).save(captor.capture());

                Volunteer capturedVolunteer = captor.getValue();
                assertEquals(1L, capturedVolunteer.getUserId(), "User ID from auth response should be set");
                assertEquals("John", capturedVolunteer.getFirstName(), "First name should match request");
                assertEquals("Doe", capturedVolunteer.getLastName(), "Last name should match request");
                assertEquals(LocalDate.of(1990, 5, 15), capturedVolunteer.getDateOfBirth(), "Date of birth should match");
                assertEquals("555-1234", capturedVolunteer.getPhoneNumber(), "Phone number should match");
            }

            @Test
            @DisplayName("Should register volunteer with various valid data")
            void shouldRegisterVolunteerWithVariousValidData() {
                VolunteerRegisterRequest anotherRequest = new VolunteerRegisterRequest(
                        "Jane",
                        "Smith",
                        "jane.smith@example.com",
                        "secure123",
                        LocalDate.of(1995, 3, 20),
                        "555-5678"
                );

                AuthRegistrationResponse anotherResponse = new AuthRegistrationResponse();
                anotherResponse.setUserId(2L);

                when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                        .thenReturn(anotherResponse);

                assertDoesNotThrow(() ->
                    registrationService.registerVolunteer(anotherRequest)
                );

                verify(authClient, times(1)).registerVolunteerInAuthService(any(AuthRegistrationRequest.class));
                verify(volunteerRepository, times(1)).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should call repository save after auth service call")
            void shouldCallRepositorySaveAfterAuthServiceCall() {
                InOrder inOrder = inOrder(authClient, volunteerRepository);

                registrationService.registerVolunteer(validRegistrationRequest);

                inOrder.verify(authClient).registerVolunteerInAuthService(any(AuthRegistrationRequest.class));
                inOrder.verify(volunteerRepository).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should handle registration with special characters in name")
            void shouldHandleRegistrationWithSpecialCharactersInName() {
                VolunteerRegisterRequest requestWithSpecialChars = new VolunteerRegisterRequest(
                        "Jean-Pierre",
                        "O'Connor",
                        "jp.oconnor@example.com",
                        "password123",
                        LocalDate.of(1990, 5, 15),
                        "555-1234"
                );

                when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                        .thenReturn(authRegistrationResponse);

                assertDoesNotThrow(() ->
                    registrationService.registerVolunteer(requestWithSpecialChars)
                );

                verify(volunteerRepository, times(1)).save(any(Volunteer.class));
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @BeforeEach
            void setUp() {
                System.out.println("--- BEGIN OF NEGATIVE REGISTRATION TEST ---");
            }

            @AfterEach
            void tearDown() {
                System.out.println("--- END OF NEGATIVE REGISTRATION TEST ---");
            }

            @Test
            @DisplayName("Should throw exception on null request")
            @SuppressWarnings("ConstantConditions")
            void shouldThrowExceptionOnNullRequest() {
                assertThrows(
                        NullPointerException.class,
                        () -> registrationService.registerVolunteer(null),
                        "Should throw NullPointerException for null request"
                );

                verify(authClient, never()).registerVolunteerInAuthService(any(AuthRegistrationRequest.class));
                verify(volunteerRepository, never()).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should throw exception when auth service fails")
            void shouldThrowExceptionWhenAuthServiceFails() {
                when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                        .thenThrow(new RuntimeException("Auth service unavailable"));

                assertThrows(
                        RuntimeException.class,
                        () -> registrationService.registerVolunteer(validRegistrationRequest),
                        "Should throw exception when auth service fails"
                );

                verify(authClient, times(1)).registerVolunteerInAuthService(any(AuthRegistrationRequest.class));
                verify(volunteerRepository, never()).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should not save when auth service call fails")
            void shouldNotSaveWhenAuthServiceFails() {
                when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                        .thenThrow(new RuntimeException("Auth service error"));

                try {
                    registrationService.registerVolunteer(validRegistrationRequest);
                } catch (RuntimeException e) {
                    // Expected
                }

                verify(volunteerRepository, never()).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should throw exception when repository save fails")
            void shouldThrowExceptionWhenRepositorySaveFails() {
                when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                        .thenReturn(authRegistrationResponse);
                when(volunteerRepository.save(any(Volunteer.class)))
                        .thenThrow(new RuntimeException("Database error"));

                assertThrows(
                        RuntimeException.class,
                        () -> registrationService.registerVolunteer(validRegistrationRequest),
                        "Should throw exception when repository save fails"
                );

                verify(authClient, times(1)).registerVolunteerInAuthService(any(AuthRegistrationRequest.class));
                verify(volunteerRepository, times(1)).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should throw exception when auth service returns null response")
            void shouldThrowExceptionWhenAuthServiceReturnsNull() {
                when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                        .thenReturn(null);

                assertThrows(
                        NullPointerException.class,
                        () -> registrationService.registerVolunteer(validRegistrationRequest),
                        "Should throw exception when auth service returns null"
                );

                verify(volunteerRepository, never()).save(any(Volunteer.class));
            }

            @Test
            @DisplayName("Should handle VolunteerAlreadyExistsException from repository")
            void shouldHandleVolunteerAlreadyExistsException() {
                when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                        .thenReturn(authRegistrationResponse);
                when(volunteerRepository.save(any(Volunteer.class)))
                        .thenThrow(new VolunteerAlreadyExistsException("Volunteer already exists", null));

                assertThrows(
                        VolunteerAlreadyExistsException.class,
                        () -> registrationService.registerVolunteer(validRegistrationRequest)
                );

                verify(authClient, times(1)).registerVolunteerInAuthService(any(AuthRegistrationRequest.class));
                verify(volunteerRepository, times(1)).save(any(Volunteer.class));
            }
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @BeforeEach
        void setUp() {
            System.out.println("--- BEGIN OF EDGE CASE TEST ---");
        }

        @AfterEach
        void tearDown() {
            System.out.println("--- END OF EDGE CASE TEST ---");
        }

        @Test
        @DisplayName("Should handle registration with minimal valid data")
        void shouldHandleRegistrationWithMinimalValidData() {
            VolunteerRegisterRequest minimalRequest = new VolunteerRegisterRequest(
                    "A",
                    "B",
                    "a@example.com",
                    "p",
                    LocalDate.of(2000, 1, 1),
                    "1"
            );

            when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);
            when(volunteerRepository.save(any(Volunteer.class)))
                    .thenReturn(savedVolunteer);

            assertDoesNotThrow(() ->
                registrationService.registerVolunteer(minimalRequest)
            );

            verify(volunteerRepository, times(1)).save(any(Volunteer.class));
        }

        @Test
        @DisplayName("Should handle registration with long strings")
        void shouldHandleRegistrationWithLongStrings() {
            String longString = "a".repeat(100);
            VolunteerRegisterRequest longStringRequest = new VolunteerRegisterRequest(
                    longString,
                    longString,
                    longString + "@example.com",
                    longString,
                    LocalDate.of(1990, 5, 15),
                    "555-1234"
            );

            when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);
            when(volunteerRepository.save(any(Volunteer.class)))
                    .thenReturn(savedVolunteer);

            assertDoesNotThrow(() ->
                registrationService.registerVolunteer(longStringRequest)
            );

            verify(volunteerRepository, times(1)).save(any(Volunteer.class));
        }

        @Test
        @DisplayName("Should register volunteer with future birth date")
        void shouldRegisterVolunteerWithFutureBirthDate() {
            VolunteerRegisterRequest futureDateRequest = new VolunteerRegisterRequest(
                    "John",
                    "Doe",
                    "john.doe@example.com",
                    "password123",
                    LocalDate.of(2030, 5, 15),
                    "555-1234"
            );

            when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);
            when(volunteerRepository.save(any(Volunteer.class)))
                    .thenReturn(savedVolunteer);

            // Should not throw, business logic validation is not in RegistrationService
            assertDoesNotThrow(() ->
                registrationService.registerVolunteer(futureDateRequest)
            );

            verify(volunteerRepository, times(1)).save(any(Volunteer.class));
        }

        @Test
        @DisplayName("Should call auth client with VOLUNTEER role")
        void shouldCallAuthClientWithVolunteerRole() {
            when(authClient.registerVolunteerInAuthService(any(AuthRegistrationRequest.class)))
                    .thenReturn(authRegistrationResponse);
            when(volunteerRepository.save(any(Volunteer.class)))
                    .thenReturn(savedVolunteer);

            registrationService.registerVolunteer(validRegistrationRequest);

            ArgumentCaptor<AuthRegistrationRequest> captor = ArgumentCaptor.forClass(AuthRegistrationRequest.class);
            verify(authClient).registerVolunteerInAuthService(captor.capture());

            assertEquals("VOLUNTEER", captor.getValue().getRole(), "Role should always be VOLUNTEER");
        }
    }
}

