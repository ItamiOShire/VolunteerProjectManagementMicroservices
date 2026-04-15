package com.vpm.taskserver.unit.controller;

import com.vpm.taskserver.controller.VolunteerTaskController;
import com.vpm.taskserver.dto.template.TaskTemplate;
import com.vpm.taskserver.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VolunteerTaskController.class)
class VolunteerTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private TaskTemplate taskTemplate;

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_ID_VALUE = "1";
    private static final String USER_ROLE_VALUE = "VOLUNTEER";

    @BeforeEach
    void setUp() {
        taskTemplate = TaskTemplate.builder()
                .itemId(1L)
                .title("Volunteer Task 1")
                .description("Task assigned to volunteer")
                .priorityName("High")
                .deadline(LocalDate.of(2026, 5, 1))
                .build();
    }

    @Nested
    @DisplayName("GetAllVolunteerTasksTests")
    class GetAllVolunteerTasksTests {
        @Test
        @DisplayName("GET /api/volunteers/{id}/tasks - Should return all volunteer tasks successfully")
        void testGetAllVolunteerTasks_Success() throws Exception {
            long volunteerId = 1L;
            TaskTemplate task2 = TaskTemplate.builder()
                    .itemId(2L)
                    .title("Volunteer Task 2")
                    .description("Another task for volunteer")
                    .priorityName("Medium")
                    .deadline(LocalDate.of(2026, 5, 15))
                    .build();

            when(taskService.getAllVolunteerTasks(volunteerId))
                    .thenReturn(Arrays.asList(taskTemplate, task2));

            mockMvc.perform(get("/api/volunteers/{id}/tasks", volunteerId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].itemId", is(1)))
                    .andExpect(jsonPath("$[0].title", is("Volunteer Task 1")))
                    .andExpect(jsonPath("$[1].itemId", is(2)))
                    .andExpect(jsonPath("$[1].title", is("Volunteer Task 2")));

            verify(taskService, times(1)).getAllVolunteerTasks(volunteerId);
        }

        @Test
        @DisplayName("GET /api/volunteers/{id}/tasks - Should return empty list when volunteer has no tasks")
        void testGetAllVolunteerTasks_EmptyList() throws Exception {
            long volunteerId = 999L;
            when(taskService.getAllVolunteerTasks(volunteerId))
                    .thenReturn(new ArrayList<>());

            mockMvc.perform(get("/api/volunteers/{id}/tasks", volunteerId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(taskService, times(1)).getAllVolunteerTasks(volunteerId);
        }

        @Test
        @DisplayName("GET /api/volunteers/{id}/tasks - Should return 400 when X-User-Id header is missing")
        void testGetAllVolunteerTasks_MissingUserIdHeader() throws Exception {
            mockMvc.perform(get("/api/volunteers/{id}/tasks", 1L)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/volunteers/{id}/tasks - Should return 400 when X-User-Role header is missing")
        void testGetAllVolunteerTasks_MissingUserRoleHeader() throws Exception {
            mockMvc.perform(get("/api/volunteers/{id}/tasks", 1L)
                            .header(USER_ID_HEADER, USER_ID_VALUE))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GetVolunteerTasksInProjectTests")
    class GetVolunteerTasksInProjectTests {
        @Test
        @DisplayName("GET /api/volunteers/{volunteerId}/projects/{projectId}/tasks - Should return tasks successfully")
        void testGetAllVolunteerTasksInProject_Success() throws Exception {
            long volunteerId = 1L;
            long projectId = 100L;

            TaskTemplate task2 = TaskTemplate.builder()
                    .itemId(2L)
                    .title("Task in Project")
                    .description("Task in specific project")
                    .priorityName("Low")
                    .deadline(LocalDate.of(2026, 5, 15))
                    .build();

            when(taskService.getAllVolunteerTasksInProject(volunteerId, projectId))
                    .thenReturn(Arrays.asList(taskTemplate, task2));

            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks",
                            volunteerId, projectId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].itemId", is(1)))
                    .andExpect(jsonPath("$[1].itemId", is(2)));

            verify(taskService, times(1)).getAllVolunteerTasksInProject(volunteerId, projectId);
        }

        @Test
        @DisplayName("GET /api/volunteers/{volunteerId}/projects/{projectId}/tasks - Should return empty list")
        void testGetAllVolunteerTasksInProject_EmptyList() throws Exception {
            long volunteerId = 1L;
            long projectId = 100L;

            when(taskService.getAllVolunteerTasksInProject(volunteerId, projectId))
                    .thenReturn(new ArrayList<>());

            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks",
                            volunteerId, projectId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(taskService, times(1)).getAllVolunteerTasksInProject(volunteerId, projectId);
        }

        @Test
        @DisplayName("GET /api/volunteers/{volunteerId}/projects/{projectId}/tasks - Should return single task with correct structure")
        void testGetAllVolunteerTasksInProject_SingleTask() throws Exception {
            long volunteerId = 1L;
            long projectId = 100L;

            when(taskService.getAllVolunteerTasksInProject(volunteerId, projectId))
                    .thenReturn(Collections.singletonList(taskTemplate));

            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks",
                            volunteerId, projectId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].itemId", is(1)))
                    .andExpect(jsonPath("$[0].title", is("Volunteer Task 1")))
                    .andExpect(jsonPath("$[0].priorityName", is("High")));

            verify(taskService, times(1)).getAllVolunteerTasksInProject(volunteerId, projectId);
        }

        @Test
        @DisplayName("GET /api/volunteers/{volunteerId}/projects/{projectId}/tasks - Should return 400 when X-User-Id header is missing")
        void testGetAllVolunteerTasksInProject_MissingUserIdHeader() throws Exception {
            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks", 1L, 100L)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/volunteers/{volunteerId}/projects/{projectId}/tasks - Should return 400 when X-User-Role header is missing")
        void testGetAllVolunteerTasksInProject_MissingUserRoleHeader() throws Exception {
            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks", 1L, 100L)
                            .header(USER_ID_HEADER, USER_ID_VALUE))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GetTaskSuggestionsTests")
    class GetTaskSuggestionsTests {
        @Test
        @DisplayName("GET /api/volunteers/{volunteerId}/projects/{projectId}/tasks/suggestions - Should return suggestions successfully")
        void testGetAllVolunteerTasksSuggestionsInProject_Success() throws Exception {
            long volunteerId = 1L;
            long projectId = 100L;

            TaskTemplate suggestion1 = TaskTemplate.builder()
                    .itemId(3L)
                    .title("Suggested Task 1")
                    .description("Suggestion for volunteer")
                    .priorityName("High")
                    .deadline(LocalDate.of(2026, 6, 1))
                    .build();

            TaskTemplate suggestion2 = TaskTemplate.builder()
                    .itemId(4L)
                    .title("Suggested Task 2")
                    .description("Another suggestion")
                    .priorityName("Medium")
                    .deadline(LocalDate.of(2026, 6, 15))
                    .build();

            when(taskService.getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId))
                    .thenReturn(Arrays.asList(suggestion1, suggestion2));

            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks/suggestions",
                            volunteerId, projectId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].itemId", is(3)))
                    .andExpect(jsonPath("$[0].title", is("Suggested Task 1")))
                    .andExpect(jsonPath("$[1].itemId", is(4)))
                    .andExpect(jsonPath("$[1].title", is("Suggested Task 2")));

            verify(taskService, times(1)).getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId);
        }

        @Test
        @DisplayName("GET /api/volunteers/{volunteerId}/projects/{projectId}/tasks/suggestions - Should return empty list when no suggestions")
        void testGetAllVolunteerTasksSuggestionsInProject_EmptyList() throws Exception {
            long volunteerId = 1L;
            long projectId = 100L;

            when(taskService.getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId))
                    .thenReturn(new ArrayList<>());

            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks/suggestions",
                            volunteerId, projectId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(taskService, times(1)).getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId);
        }

        @Test
        @DisplayName("GET /api/volunteers/{volunteerId}/projects/{projectId}/tasks/suggestions - Should return single suggestion with correct structure")
        void testGetAllVolunteerTasksSuggestionsInProject_SingleSuggestion() throws Exception {
            long volunteerId = 1L;
            long projectId = 100L;

            TaskTemplate suggestion = TaskTemplate.builder()
                    .itemId(3L)
                    .title("Suggested Task")
                    .description("Single suggestion")
                    .priorityName("Low")
                    .deadline(LocalDate.of(2026, 7, 1))
                    .build();

            when(taskService.getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId))
                    .thenReturn(Collections.singletonList(suggestion));

            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks/suggestions",
                            volunteerId, projectId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].itemId", is(3)))
                    .andExpect(jsonPath("$[0].title", is("Suggested Task")))
                    .andExpect(jsonPath("$[0].priorityName", is("Low")));

            verify(taskService, times(1)).getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId);
        }

        @Test
        @DisplayName("GET /api/volunteers/{volunteerId}/projects/{projectId}/tasks/suggestions - Should return 400 when X-User-Id header is missing")
        void testGetAllVolunteerTasksSuggestionsInProject_MissingUserIdHeader() throws Exception {
            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks/suggestions",
                            1L, 100L)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/volunteers/{volunteerId}/projects/{projectId}/tasks/suggestions - Should return 400 when X-User-Role header is missing")
        void testGetAllVolunteerTasksSuggestionsInProject_MissingUserRoleHeader() throws Exception {
            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks/suggestions",
                            1L, 100L)
                            .header(USER_ID_HEADER, USER_ID_VALUE))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("IntegrationTests")
    class IntegrationTests {
        @Test
        @DisplayName("Should handle multiple volunteers with independent data")
        void testMultipleVolunteersIndependentData() throws Exception {
            long volunteer1 = 1L;
            long volunteer2 = 2L;

            TaskTemplate volunteer1Task = TaskTemplate.builder()
                    .itemId(1L)
                    .title("Task for Volunteer 1")
                    .description("Volunteer 1 task")
                    .priorityName("High")
                    .deadline(LocalDate.of(2026, 5, 1))
                    .build();

            TaskTemplate volunteer2Task = TaskTemplate.builder()
                    .itemId(2L)
                    .title("Task for Volunteer 2")
                    .description("Volunteer 2 task")
                    .priorityName("Low")
                    .deadline(LocalDate.of(2026, 6, 1))
                    .build();

            when(taskService.getAllVolunteerTasks(volunteer1))
                    .thenReturn(Collections.singletonList(volunteer1Task));
            when(taskService.getAllVolunteerTasks(volunteer2))
                    .thenReturn(Collections.singletonList(volunteer2Task));

            mockMvc.perform(get("/api/volunteers/{id}/tasks", volunteer1)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title", is("Task for Volunteer 1")));

            mockMvc.perform(get("/api/volunteers/{id}/tasks", volunteer2)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title", is("Task for Volunteer 2")));

            verify(taskService, times(1)).getAllVolunteerTasks(volunteer1);
            verify(taskService, times(1)).getAllVolunteerTasks(volunteer2);
        }

        @Test
        @DisplayName("Should differentiate between tasks in specific project vs all tasks")
        void testGetTasksInProjectVsGetAllTasks() throws Exception {
            long volunteerId = 1L;
            long projectId = 100L;

            TaskTemplate taskInProject = TaskTemplate.builder()
                    .itemId(1L)
                    .title("Task in Project 100")
                    .description("Task in specific project")
                    .priorityName("High")
                    .deadline(LocalDate.of(2026, 5, 1))
                    .build();

            when(taskService.getAllVolunteerTasksInProject(volunteerId, projectId))
                    .thenReturn(Collections.singletonList(taskInProject));
            when(taskService.getAllVolunteerTasks(volunteerId))
                    .thenReturn(Arrays.asList(taskInProject,
                            TaskTemplate.builder()
                                    .itemId(2L)
                                    .title("Task in Another Project")
                                    .description("Task in different project")
                                    .priorityName("Low")
                                    .deadline(LocalDate.of(2026, 6, 1))
                                    .build()));

            mockMvc.perform(get("/api/volunteers/{volunteerId}/projects/{projectId}/tasks",
                            volunteerId, projectId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title", is("Task in Project 100")));

            mockMvc.perform(get("/api/volunteers/{id}/tasks", volunteerId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(taskService, times(1)).getAllVolunteerTasksInProject(volunteerId, projectId);
            verify(taskService, times(1)).getAllVolunteerTasks(volunteerId);
        }
    }
}


