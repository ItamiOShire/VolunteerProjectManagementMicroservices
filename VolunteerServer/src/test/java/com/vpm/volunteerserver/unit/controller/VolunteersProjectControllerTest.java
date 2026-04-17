package com.vpm.volunteerserver.unit.controller;

import com.vpm.volunteerserver.controller.VolunteersProjectController;
import com.vpm.volunteerserver.dto.response.VolunteerProfileResponse;
import com.vpm.volunteerserver.security.RequestFilter;
import com.vpm.volunteerserver.service.VolunteerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(VolunteersProjectController.class)
@Import(RequestFilter.class)
public class VolunteersProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    private List<VolunteerProfileResponse> volunteerList;
    private VolunteerProfileResponse volunteer1;

    @BeforeEach
    void setUp() {
        volunteer1 = VolunteerProfileResponse.builder()
                .fullName("John Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .contact("555-1234")
                .build();

        VolunteerProfileResponse volunteer2 = VolunteerProfileResponse.builder()
                .fullName("Jane Smith")
                .dateOfBirth(LocalDate.of(1995, 3, 20))
                .contact("555-9999")
                .build();

        volunteerList = new ArrayList<>();
        volunteerList.add(volunteer1);
        volunteerList.add(volunteer2);
    }

    @Nested
    @DisplayName("GET /api/projects/{projectId}/volunteers - Get Volunteers in Project Tests")
    class GetVolunteersInProjectTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully retrieve volunteers in project")
            void shouldSuccessfullyRetrieveVolunteersInProject() throws Exception {
                Long projectId = 1L;

                when(volunteerService.getVolunteersInProject(projectId))
                        .thenReturn(volunteerList);

                mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2))
                        .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                        .andExpect(jsonPath("$[1].fullName").value("Jane Smith"));

                verify(volunteerService, times(1)).getVolunteersInProject(projectId);
            }

            @Test
            @DisplayName("Should retrieve empty list when no volunteers in project")
            void shouldRetrieveEmptyListWhenNoVolunteersInProject() throws Exception {
                Long projectId = 1L;

                when(volunteerService.getVolunteersInProject(projectId))
                        .thenReturn(new ArrayList<>());

                mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(0));

                verify(volunteerService, times(1)).getVolunteersInProject(projectId);
            }

            @Test
            @DisplayName("Should retrieve single volunteer in project")
            void shouldRetrieveSingleVolunteerInProject() throws Exception {
                Long projectId = 1L;
                List<VolunteerProfileResponse> singleVolunteer = new ArrayList<>();
                singleVolunteer.add(volunteer1);

                when(volunteerService.getVolunteersInProject(projectId))
                        .thenReturn(singleVolunteer);

                mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(1))
                        .andExpect(jsonPath("$[0].fullName").value("John Doe"));

                verify(volunteerService, times(1)).getVolunteersInProject(projectId);
            }

            @Test
            @DisplayName("Should retrieve volunteers with correct fields")
            void shouldRetrieveVolunteersWithCorrectFields() throws Exception {
                Long projectId = 1L;

                when(volunteerService.getVolunteersInProject(projectId))
                        .thenReturn(volunteerList);

                mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].dateOfBirth").value("1990-05-15"))
                        .andExpect(jsonPath("$[0].contact").value("555-1234"))
                        .andExpect(jsonPath("$[1].dateOfBirth").value("1995-03-20"))
                        .andExpect(jsonPath("$[1].contact").value("555-9999"));

                verify(volunteerService, times(1)).getVolunteersInProject(projectId);
            }

            @Test
            @DisplayName("Should retrieve volunteers for different project IDs")
            void shouldRetrieveVolunteersForDifferentProjectIds() throws Exception {
                Long projectId = 5L;

                when(volunteerService.getVolunteersInProject(projectId))
                        .thenReturn(volunteerList);

                mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2));

                verify(volunteerService, times(1)).getVolunteersInProject(projectId);
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @Test
            @DisplayName("Should return 400 when missing X-User-Id header")
            void shouldReturn400WhenMissingUserIdHeader() throws Exception {
                Long projectId = 1L;

                mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteersInProject(any());
            }

            @Test
            @DisplayName("Should return 400 when missing X-User-Role header")
            void shouldReturn400WhenMissingUserRoleHeader() throws Exception {
                Long projectId = 1L;

                mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteersInProject(any());
            }

            @Test
            @DisplayName("Should return 400 when both headers are missing")
            void shouldReturn400WhenBothHeadersMissing() throws Exception {
                Long projectId = 1L;

                mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteersInProject(any());
            }

            @Test
            @DisplayName("Should return 400 when headers are empty")
            void shouldReturn400WhenHeadersAreEmpty() throws Exception {
                Long projectId = 1L;

                mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId)
                        .header(USER_ID_HEADER, "")
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteersInProject(any());
            }

            @Test
            @DisplayName("Should handle service throwing exception")
            void shouldHandleServiceThrowingException() throws Exception {
                Long projectId = 1L;

                when(volunteerService.getVolunteersInProject(projectId))
                        .thenThrow(new RuntimeException("Service error"));

                mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isInternalServerError());

                verify(volunteerService, times(1)).getVolunteersInProject(projectId);
            }
        }
    }

    @Nested
    @DisplayName("Header validation tests")
    class HeaderValidationTests {

        @Test
        @DisplayName("Should accept requests with valid headers")
        void shouldAcceptRequestsWithValidHeaders() throws Exception {
            Long projectId = 1L;

            when(volunteerService.getVolunteersInProject(projectId))
                    .thenReturn(volunteerList);

            mockMvc.perform(get("/api/projects/{projectId}/volunteers", projectId)
                    .header(USER_ID_HEADER, "123")
                    .header(USER_ROLE_HEADER, "ORGANIZATION"))
                    .andExpect(status().isOk());

            verify(volunteerService, times(1)).getVolunteersInProject(projectId);
        }
    }
}



