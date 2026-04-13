package com.vpm.volunteerserver.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.volunteerserver.controller.VolunteerController;
import com.vpm.volunteerserver.dto.request.VolunteerRegisterRequest;
import com.vpm.volunteerserver.dto.response.VolunteerProfileResponse;
import com.vpm.volunteerserver.exception.volunteer.NoSuchVolunteerException;
import com.vpm.volunteerserver.security.RequestFilter;
import com.vpm.volunteerserver.service.RegistrationService;
import com.vpm.volunteerserver.service.VolunteerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


// TODO: implement invalid json test in every module
// TODO: seed all databases

@WebMvcTest(VolunteerController.class)
@Import(RequestFilter.class)
@SuppressWarnings("unchecked")
public class VolunteerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private VolunteerService volunteerService;

    /*
     * Required headers for all requests
     */
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String VALID_USER_ID = "1";
    private static final String VALID_USER_ROLE = "VOLUNTEER";

    /*
     * Test data
     */
    private VolunteerRegisterRequest validRegisterRequest;
    private VolunteerProfileResponse validProfileResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new VolunteerRegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                LocalDate.of(1990, 5, 15),
                "555-1234"
        );

        validProfileResponse = VolunteerProfileResponse.builder()
                .fullName("John Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .contact("555-1234")
                .build();
    }

    @Nested
    @DisplayName("GET /api/volunteers/{id} - Get Volunteer Profile Tests")
    class GetVolunteerProfileTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully retrieve volunteer profile with valid id")
            void shouldSuccessfullyRetrieveVolunteerProfile() throws Exception {
                Long volunteerUserId = 1L;

                when(volunteerService.getVolunteerProfile(volunteerUserId))
                        .thenReturn(validProfileResponse);

                mockMvc.perform(get("/api/volunteers/{id}", volunteerUserId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.fullName").value("John Doe"))
                        .andExpect(jsonPath("$.contact").value("555-1234"));

                verify(volunteerService, times(1)).getVolunteerProfile(volunteerUserId);
            }

            @Test
            @DisplayName("Should retrieve profile with correct date of birth")
            void shouldRetrieveProfileWithCorrectDateOfBirth() throws Exception {
                Long volunteerUserId = 1L;

                when(volunteerService.getVolunteerProfile(volunteerUserId))
                        .thenReturn(validProfileResponse);

                mockMvc.perform(get("/api/volunteers/{id}", volunteerUserId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.dateOfBirth").value("1990-05-15"));

                verify(volunteerService, times(1)).getVolunteerProfile(volunteerUserId);
            }

            @Test
            @DisplayName("Should retrieve profile for different volunteer IDs")
            void shouldRetrieveProfileForDifferentVolunteerIds() throws Exception {
                Long volunteerId = 5L;
                VolunteerProfileResponse anotherProfile = VolunteerProfileResponse.builder()
                        .fullName("Jane Smith")
                        .dateOfBirth(LocalDate.of(1995, 3, 20))
                        .contact("555-9999")
                        .build();

                when(volunteerService.getVolunteerProfile(volunteerId))
                        .thenReturn(anotherProfile);

                mockMvc.perform(get("/api/volunteers/{id}", volunteerId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.fullName").value("Jane Smith"));

                verify(volunteerService, times(1)).getVolunteerProfile(volunteerId);
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @Test
            @DisplayName("Should return 404 when volunteer not found")
            void shouldReturn404WhenVolunteerNotFound() throws Exception {
                Long nonExistentVolunteerId = 999L;

                when(volunteerService.getVolunteerProfile(nonExistentVolunteerId))
                        .thenThrow(new NoSuchVolunteerException(nonExistentVolunteerId));

                mockMvc.perform(get("/api/volunteers/{id}", nonExistentVolunteerId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isNotFound());

                verify(volunteerService, times(1)).getVolunteerProfile(nonExistentVolunteerId);
            }

            @Test
            @DisplayName("Should return 400 when missing X-User-Id header")
            void shouldReturn400WhenMissingUserIdHeader() throws Exception {
                Long volunteerUserId = 1L;

                mockMvc.perform(get("/api/volunteers/{id}", volunteerUserId)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteerProfile(any());
            }

            @Test
            @DisplayName("Should return 400 when missing X-User-Role header")
            void shouldReturn400WhenMissingUserRoleHeader() throws Exception {
                Long volunteerUserId = 1L;

                mockMvc.perform(get("/api/volunteers/{id}", volunteerUserId)
                        .header(USER_ID_HEADER, VALID_USER_ID))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteerProfile(any());
            }

            @Test
            @DisplayName("Should return 400 when both headers are missing")
            void shouldReturn400WhenBothHeadersMissing() throws Exception {
                Long volunteerUserId = 1L;

                mockMvc.perform(get("/api/volunteers/{id}", volunteerUserId))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteerProfile(any());
            }

            @Test
            @DisplayName("Should return 400 when headers are empty")
            void shouldReturn400WhenHeadersAreEmpty() throws Exception {
                Long volunteerUserId = 1L;

                mockMvc.perform(get("/api/volunteers/{id}", volunteerUserId)
                        .header(USER_ID_HEADER, "")
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteerProfile(any());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/volunteers/register - Register Volunteer Tests")
    class RegisterVolunteerTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully register volunteer with valid data")
            void shouldSuccessfullyRegisterVolunteer() throws Exception {
                doNothing().when(registrationService).registerVolunteer(any(VolunteerRegisterRequest.class));

                mockMvc.perform(post("/api/volunteers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Volunteer registered successfully"));

                verify(registrationService, times(1)).registerVolunteer(any(VolunteerRegisterRequest.class));
            }

            @Test
            @DisplayName("Should register volunteer with special characters in name")
            void shouldRegisterVolunteerWithSpecialCharactersInName() throws Exception {
                VolunteerRegisterRequest requestWithSpecialChars = new VolunteerRegisterRequest(
                        "Jean-Pierre",
                        "O'Connor",
                        "jp.oconnor@example.com",
                        "password123",
                        LocalDate.of(1990, 5, 15),
                        "555-1234"
                );

                doNothing().when(registrationService).registerVolunteer(any(VolunteerRegisterRequest.class));

                mockMvc.perform(post("/api/volunteers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithSpecialChars))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk());

                verify(registrationService, times(1)).registerVolunteer(any(VolunteerRegisterRequest.class));
            }

            @Test
            @DisplayName("Should register multiple volunteers sequentially")
            void shouldRegisterMultipleVolunteersSequentially() throws Exception {
                VolunteerRegisterRequest request1 = new VolunteerRegisterRequest(
                        "John", "Doe", "john@example.com", "pass", LocalDate.of(1990, 1, 1), "111"
                );
                VolunteerRegisterRequest request2 = new VolunteerRegisterRequest(
                        "Jane", "Smith", "jane@example.com", "pass", LocalDate.of(1995, 1, 1), "222"
                );

                doNothing().when(registrationService).registerVolunteer(any(VolunteerRegisterRequest.class));

                mockMvc.perform(post("/api/volunteers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk());

                mockMvc.perform(post("/api/volunteers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk());

                verify(registrationService, times(2)).registerVolunteer(any(VolunteerRegisterRequest.class));
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @Test
            @DisplayName("Should return 400 when missing X-User-Id header")
            void shouldReturn400WhenMissingUserIdHeader() throws Exception {
                mockMvc.perform(post("/api/volunteers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest))
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(registrationService, never()).registerVolunteer(any());
            }

            @Test
            @DisplayName("Should return 400 when missing X-User-Role header")
            void shouldReturn400WhenMissingUserRoleHeader() throws Exception {
                mockMvc.perform(post("/api/volunteers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest))
                        .header(USER_ID_HEADER, VALID_USER_ID))
                        .andExpect(status().isBadRequest());

                verify(registrationService, never()).registerVolunteer(any());
            }

            @Test
            @DisplayName("Should return 400 when both headers are missing")
            void shouldReturn400WhenBothHeadersMissing() throws Exception {
                mockMvc.perform(post("/api/volunteers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                        .andExpect(status().isBadRequest());

                verify(registrationService, never()).registerVolunteer(any());
            }

            @Test
            @DisplayName("Should return 400 when invalid JSON body")
            void shouldReturn400WhenInvalidJsonBody() throws Exception {
                mockMvc.perform(post("/api/volunteers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalidJson")
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(registrationService, never()).registerVolunteer(any());
            }

            @Test
            @DisplayName("Should return 400 when empty body")
            void shouldReturn400WhenEmptyBody() throws Exception {
                mockMvc.perform(post("/api/volunteers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(registrationService, never()).registerVolunteer(any());
            }

            @Test
            @DisplayName("Should handle service throwing exception")
            void shouldHandleServiceThrowingException() throws Exception {
                doThrow(new RuntimeException("Service error"))
                        .when(registrationService).registerVolunteer(any(VolunteerRegisterRequest.class));

                mockMvc.perform(post("/api/volunteers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isInternalServerError());

                verify(registrationService, times(1)).registerVolunteer(any());
            }
        }
    }

    @Nested
    @DisplayName("PATCH /api/volunteers/{id} - Patch Volunteer Profile Tests")
    class PatchVolunteerProfileTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully patch volunteer profile with single field")
            void shouldSuccessfullyPatchVolunteerProfileWithSingleField() throws Exception {
                Long volunteerUserId = 1L;
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                doNothing().when(volunteerService).patchVolunteerProfile(any(Map.class), eq(volunteerUserId));

                mockMvc.perform(patch("/api/volunteers/{id}", volunteerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk());

                verify(volunteerService, times(1)).patchVolunteerProfile(any(Map.class), eq(volunteerUserId));
            }

            @Test
            @DisplayName("Should successfully patch volunteer profile with multiple fields")
            void shouldSuccessfullyPatchVolunteerProfileWithMultipleFields() throws Exception {
                Long volunteerUserId = 1L;
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");
                updates.put("lastName", "Smith");
                updates.put("phoneNumber", "555-9999");

                doNothing().when(volunteerService).patchVolunteerProfile(any(Map.class), eq(volunteerUserId));

                mockMvc.perform(patch("/api/volunteers/{id}", volunteerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk());

                verify(volunteerService, times(1)).patchVolunteerProfile(any(Map.class), eq(volunteerUserId));
            }

            @Test
            @DisplayName("Should successfully patch with empty updates")
            void shouldSuccessfullyPatchWithEmptyUpdates() throws Exception {
                Long volunteerUserId = 1L;
                Map<String, Object> updates = new HashMap<>();

                doNothing().when(volunteerService).patchVolunteerProfile(any(Map.class), eq(volunteerUserId));

                mockMvc.perform(patch("/api/volunteers/{id}", volunteerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk());

                verify(volunteerService, times(1)).patchVolunteerProfile(any(Map.class), eq(volunteerUserId));
            }

            @Test
            @DisplayName("Should patch volunteer for different IDs")
            void shouldPatchVolunteerForDifferentIds() throws Exception {
                Long volunteerId = 5L;
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "UpdatedName");

                doNothing().when(volunteerService).patchVolunteerProfile(any(Map.class), eq(volunteerId));

                mockMvc.perform(patch("/api/volunteers/{id}", volunteerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk());

                verify(volunteerService, times(1)).patchVolunteerProfile(any(Map.class), eq(volunteerId));
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @Test
            @DisplayName("Should return 404 when volunteer not found")
            void shouldReturn404WhenVolunteerNotFound() throws Exception {
                Long nonExistentVolunteerId = 999L;
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                doThrow(new NoSuchVolunteerException(nonExistentVolunteerId))
                        .when(volunteerService).patchVolunteerProfile(any(Map.class), eq(nonExistentVolunteerId));

                mockMvc.perform(patch("/api/volunteers/{id}", nonExistentVolunteerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isNotFound());

                verify(volunteerService, times(1)).patchVolunteerProfile(any(Map.class), eq(nonExistentVolunteerId));
            }

            @Test
            @DisplayName("Should return 400 when missing X-User-Id header")
            void shouldReturn400WhenMissingUserIdHeader() throws Exception {
                Long volunteerUserId = 1L;
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                mockMvc.perform(patch("/api/volunteers/{id}", volunteerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).patchVolunteerProfile(any(Map.class), any());
            }

            @Test
            @DisplayName("Should return 400 when missing X-User-Role header")
            void shouldReturn400WhenMissingUserRoleHeader() throws Exception {
                Long volunteerUserId = 1L;
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                mockMvc.perform(patch("/api/volunteers/{id}", volunteerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .header(USER_ID_HEADER, VALID_USER_ID))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).patchVolunteerProfile(any(Map.class), any());
            }

            @Test
            @DisplayName("Should return 400 when both headers are missing")
            void shouldReturn400WhenBothHeadersMissing() throws Exception {
                Long volunteerUserId = 1L;
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                mockMvc.perform(patch("/api/volunteers/{id}", volunteerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).patchVolunteerProfile(any(Map.class), any());
            }

            @Test
            @DisplayName("Should return 400 when invalid JSON body")
            void shouldReturn400WhenInvalidJsonBody() throws Exception {
                Long volunteerUserId = 1L;

                mockMvc.perform(patch("/api/volunteers/{id}", volunteerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalidJson")
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).patchVolunteerProfile(any(Map.class), any());
            }

            @Test
            @DisplayName("Should handle service throwing exception")
            void shouldHandleServiceThrowingException() throws Exception {
                Long volunteerUserId = 1L;
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", "Jane");

                doThrow(new RuntimeException("Service error"))
                        .when(volunteerService).patchVolunteerProfile(any(Map.class), eq(volunteerUserId));

                mockMvc.perform(patch("/api/volunteers/{id}", volunteerUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isInternalServerError());

                verify(volunteerService, times(1)).patchVolunteerProfile(any(Map.class), eq(volunteerUserId));
            }
        }
    }

    @Nested
    @DisplayName("Header validation tests")
    class HeaderValidationTests {

        @Test
        @DisplayName("Should accept requests with valid headers on GET endpoint")
        void shouldAcceptRequestsWithValidHeadersOnGetEndpoint() throws Exception {
            Long volunteerUserId = 1L;

            when(volunteerService.getVolunteerProfile(volunteerUserId))
                    .thenReturn(validProfileResponse);

            mockMvc.perform(get("/api/volunteers/{id}", volunteerUserId)
                    .header(USER_ID_HEADER, "123")
                    .header(USER_ROLE_HEADER, "ORGANIZATION"))
                    .andExpect(status().isOk());

            verify(volunteerService, times(1)).getVolunteerProfile(volunteerUserId);
        }

        @Test
        @DisplayName("Should accept requests with valid headers on POST endpoint")
        void shouldAcceptRequestsWithValidHeadersOnPostEndpoint() throws Exception {
            doNothing().when(registrationService).registerVolunteer(any(VolunteerRegisterRequest.class));

            mockMvc.perform(post("/api/volunteers/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest))
                    .header(USER_ID_HEADER, "456")
                    .header(USER_ROLE_HEADER, "ADMIN"))
                    .andExpect(status().isOk());

            verify(registrationService, times(1)).registerVolunteer(any());
        }

        @Test
        @DisplayName("Should accept requests with valid headers on PATCH endpoint")
        void shouldAcceptRequestsWithValidHeadersOnPatchEndpoint() throws Exception {
            Long volunteerUserId = 1L;
            Map<String, Object> updates = new HashMap<>();
            updates.put("firstName", "Jane");

            doNothing().when(volunteerService).patchVolunteerProfile(any(Map.class), eq(volunteerUserId));

            mockMvc.perform(patch("/api/volunteers/{id}", volunteerUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updates))
                    .header(USER_ID_HEADER, "789")
                    .header(USER_ROLE_HEADER, "VOLUNTEER"))
                    .andExpect(status().isOk());

            verify(volunteerService, times(1)).patchVolunteerProfile(any(Map.class), eq(volunteerUserId));
        }
    }
}



