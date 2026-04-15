package com.vpm.authenticationserver.integration.service;

import com.vpm.authenticationserver.config.IntegrationTestsDBConfig;
import com.vpm.authenticationserver.dto.request.LoginRequest;
import com.vpm.authenticationserver.dto.response.LoginResponse;
import com.vpm.authenticationserver.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static  org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(IntegrationTestsDBConfig.class)
public class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;


    /*
     * Testing data
     */

    private LoginRequest validLoginRequest = new LoginRequest(
            "testvolunteer@gmail.com",
            "password"
    );
    private LoginRequest invalidEmailLoginRequest = new LoginRequest(
            "invalidemail@gmail.com",
            "password"
    );
    private LoginRequest invalidPasswordLoginRequest = new LoginRequest(
            "testvolunteer@gmail.com",
            "wrongpassword"
    );

    @Nested
    @DisplayName("Log in testing")
    class LoginTests {

        @Test
        @DisplayName("Should log in user with valid credentials")
        void shouldLogInUserWithValidCredentials() {

            LoginResponse response =  authenticationService.login(validLoginRequest);

            assertNotNull(response, "response should not be null");

        }

        @Test
        @DisplayName("Response should contain access token")
        void shouldContainAccessToken() {
            LoginResponse response =  authenticationService.login(validLoginRequest);

            int minTokenLength = 500;

            assertNotNull(response.accessToken());
            assertTrue(response.accessToken().length() >= 500, "token should be long enough");
        }

        @Test
        @DisplayName("Response should contain refresh token")
        void shouldContainRefreshToken() {
            LoginResponse response =  authenticationService.login(validLoginRequest);

            int minTokenLength = 500;

            assertNotNull(response.refreshToken());
            assertTrue(response.refreshToken().length() >= 500, "token should be long enough");
        }

        @Test
        @DisplayName("Response should contain user id")
        void shouldContainUserId() {
            LoginResponse response =  authenticationService.login(validLoginRequest);

            long userId = 1L;

            assertEquals(userId, response.id(), "id should be equal to user id");
        }

        @Test
        @DisplayName("Response should contain user role")
        void shouldContainUserRole() {
            LoginResponse response =  authenticationService.login(validLoginRequest);

            String role = "VOLUNTEER";

            assertEquals(role, response.role());
        }

    }

}
