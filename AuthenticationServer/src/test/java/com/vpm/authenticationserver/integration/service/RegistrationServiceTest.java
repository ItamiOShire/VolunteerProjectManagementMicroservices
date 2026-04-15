package com.vpm.authenticationserver.integration.service;


import com.vpm.authenticationserver.config.IntegrationTestsDBConfig;
import com.vpm.authenticationserver.exception.user.UserAlreadyExistsException;
import com.vpm.authenticationserver.service.RegistrationService;
import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;


import static  org.junit.jupiter.api.Assertions.*;

@SpringBootTest
/*
 * By this import, you enable Testcontainers -> see this configuration class
 */
@Import(IntegrationTestsDBConfig.class)
public class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    /*
     * Testing data
     */

    AuthRegistrationRequest volunteerRequest = new AuthRegistrationRequest(
            "integrationtestemail@gmail.com",
            "password",
            "VOLUNTEER"
    );

    @Nested
    @DisplayName("Registration of volunteer")
    class VolunteerRegistration {

        /*
         * Positive testing
         */

        @Test
        @Transactional
        @DisplayName("Should register volunteer in database")
        void shouldRegisterVolunteerInDatabase() {

            AuthRegistrationResponse response =
                    registrationService.registerUserInAuthService(volunteerRequest);

            assertNotNull(response, "Response should not be null");

        }

        @Test
        @Transactional
        @DisplayName("Saved user should have different id")
        void shouldSaveUserHashedPassword() {

            long idBeforeSave = 0L;

            AuthRegistrationResponse response =
                    registrationService.registerUserInAuthService(volunteerRequest);

            assertNotEquals(idBeforeSave, response.getUserId(), "saved user should have different id");
        }

        @Test
        @Transactional
        @DisplayName("Should not throw error on non-existing email")
        void shouldNotThrowErrorOnNonExistingEmail() {

            assertDoesNotThrow(
                    () -> registrationService.registerUserInAuthService(volunteerRequest),
                    "should not throw error on non-existing email"
            );

        }

        /*
         * Negative testing
         */

        @Test
        @DisplayName("Should throw error on existing user registration try")
        void shouldThrowErrorOnExistingUserRegistrationTry() {

            volunteerRequest.setEmail("testvolunteer@gmail.com");

            assertThrows(
                    UserAlreadyExistsException.class,
                    () -> registrationService.registerUserInAuthService(volunteerRequest),
                    "Should throw error on existing user registration try"
            );

        }

        @Test
        @DisplayName("Should handle null request")
        void shouldHandleNullRequest() {

            assertThrows(
                    NullPointerException.class,
                    () -> registrationService.registerUserInAuthService(null),
                    "Should throw error on null request"
            );
        }

        @Test
        @Transactional
        @DisplayName("Should throw error on trying to register same user second time")
        void shouldThrowErrorOnTryToRegisterSameUserSecondTime() {

            registrationService.registerUserInAuthService(volunteerRequest);

            assertThrows(
                    UserAlreadyExistsException.class,
                    () -> registrationService.registerUserInAuthService(volunteerRequest),
                    "Should throw error on trying to register same user second time"
            );

        }

    }

}
