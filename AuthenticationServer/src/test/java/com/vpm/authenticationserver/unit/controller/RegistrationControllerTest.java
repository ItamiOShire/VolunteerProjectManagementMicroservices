package com.vpm.authenticationserver.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.authenticationserver.controller.RegistrationController;
import com.vpm.authenticationserver.security.SecurityConfig;
import com.vpm.authenticationserver.service.RegistrationService;
import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
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

@WebMvcTest(RegistrationController.class)
@Import(SecurityConfig.class)
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegistrationService registrationService;

    AuthRegistrationRequest requestVolunteer = new AuthRegistrationRequest(
            "testvolunteer@gmail.com",
            "password",
            "VOLUNTEER"
    );

    AuthRegistrationRequest requestOrganization = new AuthRegistrationRequest(
            "testorganization@gmail.com",
            "password",
            "ORGANIZATION"
    );

    AuthRegistrationResponse  responseVolunteer = new AuthRegistrationResponse(1L);
    AuthRegistrationResponse  responseOrganization = new AuthRegistrationResponse(2L);


    @Nested
    @DisplayName("POST /api/internal/registration - Volunteer registration")
    class VolunteerRegistration {

        /*
         * Positive testing
         */

        @Test
        @DisplayName("Should register volunteer")
        public void shouldRegisterVolunteer() throws Exception {

            when(registrationService.registerUserInAuthService(any(AuthRegistrationRequest.class))).thenReturn(responseVolunteer);

            mockMvc.perform(post("/api/internal/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestVolunteer))
                            .header("X-INTERNAL-REQUEST", true)
                            .header("X-SERVICE-NAME", "volunteer-service"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(objectMapper.writeValueAsString(responseVolunteer)))
                    .andExpect(jsonPath("$.userId").value(1L));

            verify(registrationService, times(1)).registerUserInAuthService(any(AuthRegistrationRequest.class));


        }

    }

    @Nested
    @DisplayName("POST /api/internal/registration - Organization registration")
    class OrganizationRegistration {

        /*
         * Positive testing
         */

        @Test
        @DisplayName("Should register organization")
        public void shouldRegisterOrganization() throws Exception {

            when(registrationService.registerUserInAuthService(any(AuthRegistrationRequest.class))).thenReturn(responseOrganization);

            mockMvc.perform(post("/api/internal/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestOrganization))
                            .header("X-INTERNAL-REQUEST", true)
                            .header("X-SERVICE-NAME", "organization-service"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(objectMapper.writeValueAsString(responseOrganization)))
                    .andExpect(jsonPath("$.userId").value(2L));

            verify(registrationService, times(1)).registerUserInAuthService(any(AuthRegistrationRequest.class));


        }
    }


    @Nested
    @DisplayName("POST /api/internal/registration - Edge Cases")
    class EdgeCases {

        /*
         * Negative testing
         */

        @Nested
        @DisplayName("Request body testing")

        class RequestBodyTest {

            @Test
            @DisplayName("Should handle invalid json")
            public void shouldHandleInvalidJson() throws Exception {

                mockMvc.perform(post("/api/internal/registration")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalidjson")
                                .header("X-INTERNAL-REQUEST", true)
                                .header("X-SERVICE-NAME", "volunteer-service"))
                        .andExpect(status().isBadRequest());

            }

            @Test
            @DisplayName("Should handle empty body")
            public void shouldHandleEmptyBody() throws Exception {

                mockMvc.perform(post("/api/internal/registration")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("")
                                .header("X-INTERNAL-REQUEST", true)
                                .header("X-SERVICE-NAME", "volunteer-service"))
                        .andExpect(status().isBadRequest());
            }
        }

        @Test
        @DisplayName("Should return forbidden status on missing headers")
        public void shouldReturnForbiddenStatusOnMissingHeaders () throws Exception {

            mockMvc.perform(post("/api/internal/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestVolunteer)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return forbidden status on missing internal request header")
        public void shouldReturnForbiddenStatusOnMissingInternalRequestHeader () throws Exception {

            mockMvc.perform(post("/api/internal/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestVolunteer))
                            .header("X-SERVICE-NAME", "volunteer-service"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return forbidden status on missing service name header")
        public void shouldReturnForbiddenStatusOnMissingServiceNameHeader () throws Exception {

            mockMvc.perform(post("/api/internal/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestVolunteer))
                            .header("X-INTERNAL-REQUEST", true))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return forbidden status on invalid value of X-INTERNAL-REQUEST")
        public void shouldReturnForbiddenStatusOnInvalidInternalRequestHeaderValue () throws Exception {

            mockMvc.perform(post("/api/internal/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestVolunteer))
                            .header("X-SERVICE-NAME", "volunteer-service")
                            .header("X-INTERNAL-REQUEST", false))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return forbidden status on invalid value of X-SERVICE-NAME")
        public void shouldReturnForbiddenStatusOnInvalidServiceNameHeaderValue () throws Exception {

            mockMvc.perform(post("/api/internal/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestVolunteer))
                            .header("X-SERVICE-NAME", "auth-service")
                            .header("X-INTERNAL-REQUEST", true))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return forbidden status on invalid value of X-SERVICE-NAME")
        public void shouldReturnForbiddenStatusOnInvalidHeadersValue () throws Exception {

            mockMvc.perform(post("/api/internal/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestVolunteer))
                            .header("X-SERVICE-NAME", "auth-service")
                            .header("X-INTERNAL-REQUEST", false))
                    .andExpect(status().isForbidden());
        }

    }

}
