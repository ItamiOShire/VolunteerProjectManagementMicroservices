package com.vpm.volunteerserver.unit.controller;

import com.vpm.volunteerserver.controller.VolunteersTasksController;
import com.vpm.volunteerserver.dto.response.VolunteerProfileResponse;
import com.vpm.volunteerserver.dto.template.VolunteerToAssignToTaskTemplate;
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


@WebMvcTest(VolunteersTasksController.class)
@Import(RequestFilter.class)
public class VolunteersTasksControllerTest {

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
    private List<VolunteerProfileResponse> assignedVolunteerList;
    private List<VolunteerToAssignToTaskTemplate> unassignedVolunteerList;
    private VolunteerProfileResponse assignedVolunteer;
    private VolunteerToAssignToTaskTemplate unassignedVolunteer1;

    @BeforeEach
    void setUp() {
        assignedVolunteer = VolunteerProfileResponse.builder()
                .fullName("John Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .contact("555-1234")
                .build();

        assignedVolunteerList = new ArrayList<>();
        assignedVolunteerList.add(assignedVolunteer);

        unassignedVolunteer1 = VolunteerToAssignToTaskTemplate.builder()
                .itemId(2L)
                .fullName("Jane Smith")
                .reportedTaskSuggestion(true)
                .build();

        VolunteerToAssignToTaskTemplate unassignedVolunteer2 = VolunteerToAssignToTaskTemplate.builder()
                .itemId(3L)
                .fullName("Bob Johnson")
                .reportedTaskSuggestion(false)
                .build();

        unassignedVolunteerList = new ArrayList<>();
        unassignedVolunteerList.add(unassignedVolunteer1);
        unassignedVolunteerList.add(unassignedVolunteer2);
    }

    @Nested
    @DisplayName("GET /api/tasks/{taskId}/projects/{projectId}/volunteers/assigned - Get Assigned Volunteers Tests")
    class GetAssignedVolunteersTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully retrieve assigned volunteers in task")
            void shouldSuccessfullyRetrieveAssignedVolunteersInTask() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                when(volunteerService.getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId))
                        .thenReturn(assignedVolunteerList);

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers/assigned", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(1))
                        .andExpect(jsonPath("$[0].fullName").value("John Doe"));

                verify(volunteerService, times(1)).getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId);
            }

            @Test
            @DisplayName("Should retrieve empty list when no assigned volunteers")
            void shouldRetrieveEmptyListWhenNoAssignedVolunteers() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                when(volunteerService.getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId))
                        .thenReturn(new ArrayList<>());

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers/assigned", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(0));

                verify(volunteerService, times(1)).getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId);
            }

            @Test
            @DisplayName("Should retrieve multiple assigned volunteers")
            void shouldRetrieveMultipleAssignedVolunteers() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                List<VolunteerProfileResponse> multipleVolunteers = new ArrayList<>();
                multipleVolunteers.add(assignedVolunteer);
                multipleVolunteers.add(VolunteerProfileResponse.builder()
                        .fullName("Jane Smith")
                        .dateOfBirth(LocalDate.of(1995, 3, 20))
                        .contact("555-9999")
                        .build());

                when(volunteerService.getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId))
                        .thenReturn(multipleVolunteers);

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers/assigned", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2));

                verify(volunteerService, times(1)).getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId);
            }

            @Test
            @DisplayName("Should retrieve volunteers with correct fields")
            void shouldRetrieveVolunteersWithCorrectFields() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                when(volunteerService.getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId))
                        .thenReturn(assignedVolunteerList);

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers/assigned", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].dateOfBirth").value("1990-05-15"))
                        .andExpect(jsonPath("$[0].contact").value("555-1234"));

                verify(volunteerService, times(1)).getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId);
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @Test
            @DisplayName("Should return 400 when missing X-User-Id header")
            void shouldReturn400WhenMissingUserIdHeader() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers/assigned", taskId, projectId)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteersInProjectWhoAreAssignedToTask(any(), any());
            }

            @Test
            @DisplayName("Should return 400 when missing X-User-Role header")
            void shouldReturn400WhenMissingUserRoleHeader() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers/assigned", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteersInProjectWhoAreAssignedToTask(any(), any());
            }

            @Test
            @DisplayName("Should return 400 when both headers are missing")
            void shouldReturn400WhenBothHeadersMissing() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers/assigned", taskId, projectId))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteersInProjectWhoAreAssignedToTask(any(), any());
            }

            @Test
            @DisplayName("Should handle service throwing exception")
            void shouldHandleServiceThrowingException() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                when(volunteerService.getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId))
                        .thenThrow(new RuntimeException("Service error"));

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers/assigned", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isInternalServerError());

                verify(volunteerService, times(1)).getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/{taskId}/projects/{projectId}/volunteers - Get Unassigned Volunteers Tests")
    class GetUnassignedVolunteersTests {

        @Nested
        @DisplayName("Positive tests")
        class PositiveTests {

            @Test
            @DisplayName("Should successfully retrieve unassigned volunteers in project by task")
            void shouldSuccessfullyRetrieveUnassignedVolunteersByTask() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                when(volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId))
                        .thenReturn(unassignedVolunteerList);

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2))
                        .andExpect(jsonPath("$[0].fullName").value("Jane Smith"))
                        .andExpect(jsonPath("$[1].fullName").value("Bob Johnson"));

                verify(volunteerService, times(1)).getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId);
            }

            @Test
            @DisplayName("Should retrieve empty list when no unassigned volunteers")
            void shouldRetrieveEmptyListWhenNoUnassignedVolunteers() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                when(volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId))
                        .thenReturn(new ArrayList<>());

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(0));

                verify(volunteerService, times(1)).getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId);
            }

            @Test
            @DisplayName("Should retrieve single unassigned volunteer")
            void shouldRetrieveSingleUnassignedVolunteer() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                List<VolunteerToAssignToTaskTemplate> singleVolunteer = new ArrayList<>();
                singleVolunteer.add(unassignedVolunteer1);

                when(volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId))
                        .thenReturn(singleVolunteer);

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(1))
                        .andExpect(jsonPath("$[0].fullName").value("Jane Smith"));

                verify(volunteerService, times(1)).getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId);
            }

            @Test
            @DisplayName("Should retrieve volunteers with reportedTaskSuggestion field")
            void shouldRetrieveVolunteersWithReportedTaskSuggestionField() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                when(volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId))
                        .thenReturn(unassignedVolunteerList);

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].reportedTaskSuggestion").value(true))
                        .andExpect(jsonPath("$[1].reportedTaskSuggestion").value(false));

                verify(volunteerService, times(1)).getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId);
            }

            @Test
            @DisplayName("Should retrieve volunteers with correct itemId")
            void shouldRetrieveVolunteersWithCorrectItemId() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                when(volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId))
                        .thenReturn(unassignedVolunteerList);

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].itemId").value(2))
                        .andExpect(jsonPath("$[1].itemId").value(3));

                verify(volunteerService, times(1)).getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId);
            }
        }

        @Nested
        @DisplayName("Negative tests")
        class NegativeTests {

            @Test
            @DisplayName("Should return 400 when missing X-User-Id header")
            void shouldReturn400WhenMissingUserIdHeader() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers", taskId, projectId)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteersNotAssignedToTaskWithTaskSuggestion(any(), any());
            }

            @Test
            @DisplayName("Should return 400 when missing X-User-Role header")
            void shouldReturn400WhenMissingUserRoleHeader() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteersNotAssignedToTaskWithTaskSuggestion(any(), any());
            }

            @Test
            @DisplayName("Should return 400 when both headers are missing")
            void shouldReturn400WhenBothHeadersMissing() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers", taskId, projectId))
                        .andExpect(status().isBadRequest());

                verify(volunteerService, never()).getVolunteersNotAssignedToTaskWithTaskSuggestion(any(), any());
            }

            @Test
            @DisplayName("Should handle service throwing exception")
            void shouldHandleServiceThrowingException() throws Exception {
                Long taskId = 1L;
                Long projectId = 1L;

                when(volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId))
                        .thenThrow(new RuntimeException("Service error"));

                mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers", taskId, projectId)
                        .header(USER_ID_HEADER, VALID_USER_ID)
                        .header(USER_ROLE_HEADER, VALID_USER_ROLE))
                        .andExpect(status().isInternalServerError());

                verify(volunteerService, times(1)).getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId);
            }
        }
    }

    @Nested
    @DisplayName("Header validation tests")
    class HeaderValidationTests {

        @Test
        @DisplayName("Should accept requests with valid headers on assigned endpoint")
        void shouldAcceptRequestsWithValidHeadersOnAssignedEndpoint() throws Exception {
            Long taskId = 1L;
            Long projectId = 1L;

            when(volunteerService.getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId))
                    .thenReturn(assignedVolunteerList);

            mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers/assigned", taskId, projectId)
                    .header(USER_ID_HEADER, "123")
                    .header(USER_ROLE_HEADER, "ORGANIZATION"))
                    .andExpect(status().isOk());

            verify(volunteerService, times(1)).getVolunteersInProjectWhoAreAssignedToTask(taskId, projectId);
        }

        @Test
        @DisplayName("Should accept requests with valid headers on unassigned endpoint")
        void shouldAcceptRequestsWithValidHeadersOnUnassignedEndpoint() throws Exception {
            Long taskId = 1L;
            Long projectId = 1L;

            when(volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId))
                    .thenReturn(unassignedVolunteerList);

            mockMvc.perform(get("/api/tasks/{taskId}/projects/{projectId}/volunteers", taskId, projectId)
                    .header(USER_ID_HEADER, "456")
                    .header(USER_ROLE_HEADER, "ADMIN"))
                    .andExpect(status().isOk());

            verify(volunteerService, times(1)).getVolunteersNotAssignedToTaskWithTaskSuggestion(taskId, projectId);
        }
    }
}



