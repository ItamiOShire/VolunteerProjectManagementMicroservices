package com.vpm.taskserver.unit.controller;

import com.vpm.taskserver.controller.ProjectTaskController;
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

@WebMvcTest(ProjectTaskController.class)
class ProjectTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private TaskTemplate taskTemplate;

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_ID_VALUE = "1";
    private static final String USER_ROLE_VALUE = "ADMIN";

    @BeforeEach
    void setUp() {
        taskTemplate = TaskTemplate.builder()
                .itemId(1L)
                .title("Project Task 1")
                .description("Task for project 100")
                .priorityName("High")
                .deadline(LocalDate.of(2026, 5, 1))
                .build();
    }

    @Nested
    @DisplayName("GetProjectTasks")
    class GetProjectTasksTests {
        @Test
        @DisplayName("GET /api/projects/{id}/tasks - Should return all project tasks successfully")
        void testGetAllProjectTasks_Success() throws Exception {
            long projectId = 100L;
            TaskTemplate task2 = TaskTemplate.builder()
                    .itemId(2L)
                    .title("Project Task 2")
                    .description("Another task for project 100")
                    .priorityName("Medium")
                    .deadline(LocalDate.of(2026, 5, 15))
                    .build();

            when(taskService.getAllProjectTasks(projectId))
                    .thenReturn(Arrays.asList(taskTemplate, task2));

            mockMvc.perform(get("/api/projects/{id}/tasks", projectId)
                    .header(USER_ID_HEADER, USER_ID_VALUE)
                    .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].itemId", is(1)))
                    .andExpect(jsonPath("$[0].title", is("Project Task 1")))
                    .andExpect(jsonPath("$[1].itemId", is(2)))
                    .andExpect(jsonPath("$[1].title", is("Project Task 2")));

            verify(taskService, times(1)).getAllProjectTasks(projectId);
        }

        @Test
        @DisplayName("GET /api/projects/{id}/tasks - Should return empty list when project has no tasks")
        void testGetAllProjectTasks_EmptyList() throws Exception {
            long projectId = 999L;
            when(taskService.getAllProjectTasks(projectId))
                    .thenReturn(new ArrayList<>());

            mockMvc.perform(get("/api/projects/{id}/tasks", projectId)
                    .header(USER_ID_HEADER, USER_ID_VALUE)
                    .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(taskService, times(1)).getAllProjectTasks(projectId);
        }

        @Test
        @DisplayName("GET /api/projects/{id}/tasks - Should return single task with correct structure")
        void testGetAllProjectTasks_SingleTask() throws Exception {
            long projectId = 100L;
            when(taskService.getAllProjectTasks(projectId))
                    .thenReturn(Collections.singletonList(taskTemplate));

            mockMvc.perform(get("/api/projects/{id}/tasks", projectId)
                    .header(USER_ID_HEADER, USER_ID_VALUE)
                    .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].itemId", is(1)))
                    .andExpect(jsonPath("$[0].title", is("Project Task 1")))
                    .andExpect(jsonPath("$[0].priorityName", is("High")))
                    .andExpect(jsonPath("$[0].deadline", is("2026-05-01")));

            verify(taskService, times(1)).getAllProjectTasks(projectId);
        }
    }

    @Nested
    @DisplayName("AuthorizationValidation")
    class AuthorizationValidationTests {
        @Test
        @DisplayName("GET /api/projects/{id}/tasks - Should return 400 when X-User-Id header is missing")
        void testGetAllProjectTasks_MissingUserIdHeader() throws Exception {
            long projectId = 100L;

            mockMvc.perform(get("/api/projects/{id}/tasks", projectId)
                    .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/projects/{id}/tasks - Should return 400 when X-User-Role header is missing")
        void testGetAllProjectTasks_MissingUserRoleHeader() throws Exception {
            long projectId = 100L;

            mockMvc.perform(get("/api/projects/{id}/tasks", projectId)
                    .header(USER_ID_HEADER, USER_ID_VALUE))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/projects/{id}/tasks - Should return 400 when both headers are missing")
        void testGetAllProjectTasks_MissingBothHeaders() throws Exception {
            long projectId = 100L;

            mockMvc.perform(get("/api/projects/{id}/tasks", projectId))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("IntegrationTests")
    class IntegrationTests {
        @Test
        @DisplayName("GET /api/projects/{id}/tasks - Should handle multiple projects with independent data")
        void testGetAllProjectTasks_MultipleProjectsIndependentData() throws Exception {
            long projectId1 = 100L;
            long projectId2 = 200L;

            TaskTemplate projectOneTask = TaskTemplate.builder()
                    .itemId(1L)
                    .title("Project 100 Task")
                    .description("Task for project 100")
                    .priorityName("High")
                    .deadline(LocalDate.of(2026, 5, 1))
                    .build();

            TaskTemplate projectTwoTask = TaskTemplate.builder()
                    .itemId(2L)
                    .title("Project 200 Task")
                    .description("Task for project 200")
                    .priorityName("Low")
                    .deadline(LocalDate.of(2026, 6, 1))
                    .build();

            when(taskService.getAllProjectTasks(projectId1))
                    .thenReturn(Collections.singletonList(projectOneTask));
            when(taskService.getAllProjectTasks(projectId2))
                    .thenReturn(Collections.singletonList(projectTwoTask));

            mockMvc.perform(get("/api/projects/{id}/tasks", projectId1)
                    .header(USER_ID_HEADER, USER_ID_VALUE)
                    .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title", is("Project 100 Task")));

            mockMvc.perform(get("/api/projects/{id}/tasks", projectId2)
                    .header(USER_ID_HEADER, USER_ID_VALUE)
                    .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title", is("Project 200 Task")));

            verify(taskService, times(1)).getAllProjectTasks(projectId1);
            verify(taskService, times(1)).getAllProjectTasks(projectId2);
        }
    }

}


