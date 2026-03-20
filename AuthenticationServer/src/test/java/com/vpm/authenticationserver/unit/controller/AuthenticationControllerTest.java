package com.vpm.authenticationserver.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.authenticationserver.controller.AuthenticationController;
import com.vpm.authenticationserver.dto.request.LoginRequest;
import com.vpm.authenticationserver.dto.response.LoginResponse;
import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.exception.user.InvalidCredentialsException;
import com.vpm.authenticationserver.security.SecurityConfig;
import com.vpm.authenticationserver.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthenticationController
 */
@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    // Test data
    private final LoginRequest validRequest = new LoginRequest(
            "testemail@gmail.com",
            "correctPassword123"
    );

    private final LoginRequest invalidEmailRequest = new LoginRequest(
            "nonexistent@gmail.com",
            "password123"
    );

    private final LoginRequest invalidPasswordRequest = new LoginRequest(
            "testemail@gmail.com",
            "wrongPassword"
    );

    private final Users volunteerUser = new Users(
            1L,
            "testemail@gmail.com",
            "encodedPassword",
            "VOLUNTEER",
            null
    );

    private final Users organizationUser = new Users(
            2L,
            "org@gmail.com",
            "encodedPassword",
            "ORGANIZATION",
            null
    );

    private final LoginResponse volunteerResponse = new LoginResponse(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.testAccessToken",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.testRefreshToken",
            volunteerUser
    );

    private final LoginResponse organizationResponse = new LoginResponse(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.testAccessToken",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.testRefreshToken",
            organizationUser
    );

    @Nested
    @DisplayName("POST /api/auth/login - Volunteer Login Tests")
    class VolunteerLoginTests {

        @Test
        @DisplayName("Should successfully login volunteer with valid credentials")
        void testSuccessfulVolunteerLogin() throws Exception {
            // Given
            when(authenticationService.login(validRequest))
                    .thenReturn(volunteerResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").value(volunteerResponse.accessToken()))
                    .andExpect(jsonPath("$.refreshToken").value(volunteerResponse.refreshToken()))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.role").value("VOLUNTEER"));

            verify(authenticationService, times(1)).login(validRequest);
        }

        @Test
        @DisplayName("Should return unauthorized when credentials are invalid")
        void testVolunteerLoginWithInvalidCredentials() throws Exception {
            // Given
            when(authenticationService.login(invalidPasswordRequest))
                    .thenThrow(new InvalidCredentialsException());

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidPasswordRequest)))
                    .andExpect(status().isUnauthorized());

            verify(authenticationService, times(1)).login(invalidPasswordRequest);
        }

        @Test
        @DisplayName("Should return unauthorized when user not found")
        void testVolunteerLoginWithNonExistentUser() throws Exception {
            // Given
            when(authenticationService.login(invalidEmailRequest))
                    .thenThrow(new InvalidCredentialsException());

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                    .andExpect(status().isUnauthorized());

            verify(authenticationService, times(1)).login(invalidEmailRequest);
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - Organization Login Tests")
    class OrganizationLoginTests {

        @Test
        @DisplayName("Should successfully login organization with valid credentials")
        void testSuccessfulOrganizationLogin() throws Exception {
            // Given
            LoginRequest orgRequest = new LoginRequest("org@gmail.com", "password123");
            when(authenticationService.login(orgRequest))
                    .thenReturn(organizationResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orgRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.role").value("ORGANIZATION"))
                    .andExpect(jsonPath("$.id").value(2L));

            verify(authenticationService, times(1)).login(orgRequest);
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle malformed request body")
        void testLoginWithMalformedJson() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"))
                    .andExpect(status().isBadRequest());

            verify(authenticationService, never()).login(any());
        }

        @Test
        @DisplayName("Should handle missing required fields")
        void testLoginWithMissingEmail() throws Exception {
            String invalidJson = "{\"password\": \"password123\"}";

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(authenticationService, never()).login(any());
        }

        @Test
        @DisplayName("Should handle missing password field")
        void testLoginWithMissingPassword() throws Exception {
            String invalidJson = "{\"email\": \"test@gmail.com\"}";

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(authenticationService, never()).login(any());
        }

        @Test
        @DisplayName("Should handle empty credentials")
        void testLoginWithEmptyCredentials() throws Exception {
            LoginRequest emptyRequest = new LoginRequest("", "");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emptyRequest)))
                    .andExpect(status().isBadRequest());

            verify(authenticationService, never()).login(any());
        }

        @Test
        @DisplayName("Should handle service exception")
        void testLoginWhenServiceThrowsException() throws Exception {
            when(authenticationService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("Unexpected error"));

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isInternalServerError());

            verify(authenticationService, times(1)).login(any());
        }
    }
}
