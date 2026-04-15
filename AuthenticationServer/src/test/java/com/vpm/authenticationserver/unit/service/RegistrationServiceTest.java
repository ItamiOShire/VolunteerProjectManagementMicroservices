package com.vpm.authenticationserver.unit.service;


import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.exception.user.UserAlreadyExistsException;
import com.vpm.authenticationserver.repository.UsersRepository;
import com.vpm.authenticationserver.service.RegistrationService;
import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    UsersRepository  usersRepository;

    @Mock
    PasswordEncoder passwordEncoder;


    @InjectMocks
    RegistrationService registrationService;

    /*
     *  Testing values
     */
    AuthRegistrationRequest volunteerRequest = new AuthRegistrationRequest(
            "testemail@gmail.com",
            "password",
            "VOLUNTEER"
    );

    AuthRegistrationRequest organizationRequest = new AuthRegistrationRequest(
            "testemail@gmail.com",
            "password",
            "ORGANIZATION"
    );

    Users volunteer = new  Users(
            0L,
            "testemail@gmail.com",
            "password",
            "VOLUNTEER",
            null
    );

    Users organization = new  Users(
            0L,
            "testemail@gmail.com",
            "password",
            "ORGANIZATION",
            null
    );

    @Nested
    @DisplayName("Testing registration of volunteer")
    public class VolunteerTesting {

        @BeforeEach
        void setUp() {
            System.out.println("--- BEGIN OF TEST ---");

            when(usersRepository.findByEmail(volunteer.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(volunteer.getPassword())).thenReturn("HASH FOR PASSWORD");
            Users savedVolunteer = volunteer;
            savedVolunteer.setId(1L);
            when(usersRepository.save(any(Users.class))).thenReturn(savedVolunteer);

        }

        @AfterEach
        void closeUp() {
            System.out.println("--- END OF TEST ---");
        }

        /*
         * Positive testing
         */

        @Test
        @DisplayName("Successful registration")
        void successfulRegistrationTest() {

            AuthRegistrationResponse response = registrationService.registerUserInAuthService(volunteerRequest);

            assertNotNull(response, "Response should not be null");

            verify(usersRepository, times(1)).findByEmail(volunteer.getEmail());
            verify(usersRepository, times(1)).save(any(Users.class));

        }

        @Test
        @DisplayName("Response should contain user id")
        void shouldSetResponseToContainUserId() {
            AuthRegistrationResponse response = registrationService.registerUserInAuthService(volunteerRequest);

            assertNotEquals(0L, response.getUserId(), "Response should not be less or equal than 0");
            assertEquals(1L,response.getUserId(), "Response should contain user id");
        }

        /*
         * Negative Testing
         */

        @Test
        @DisplayName("Should throw error on finding user with the same email")
        void shouldThrowErrorOnFindingUserWithTheSameEmail() {

            reset(usersRepository);
            reset(passwordEncoder);
            when(usersRepository.findByEmail(volunteer.getEmail())).thenReturn(Optional.of(volunteer));

            assertThrows(
                    UserAlreadyExistsException.class,
                    () -> registrationService.registerUserInAuthService(volunteerRequest)
            );

            verify(usersRepository, times(1)).findByEmail(volunteer.getEmail());

        }

        @Test
        @DisplayName("Should throw error on null request")
        void shouldThrowErrorOnNullRequest() {

            reset(usersRepository);
            reset(passwordEncoder);
            assertThrows(
                    NullPointerException.class,
                    () -> registrationService.registerUserInAuthService(null)
            );
        }

    }
}
