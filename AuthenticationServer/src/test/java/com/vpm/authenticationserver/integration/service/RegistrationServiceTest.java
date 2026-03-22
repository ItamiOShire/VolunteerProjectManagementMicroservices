package com.vpm.authenticationserver.integration.service;


import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.exception.user.UserAlreadyExistsException;
import com.vpm.authenticationserver.service.RegistrationService;
import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static  org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class RegistrationServiceTest {

    @Container
    static PostgreSQLContainer<?> database =
            new PostgreSQLContainer<>("postgres:17.9")
                    .withDatabaseName("Users")
                    .withPassword("password")
                    .withUsername("admin");


    @DynamicPropertySource
    static void configureProperties (DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.password", database::getPassword);
        registry.add("spring.datasource.username", database::getUsername);
    }

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
