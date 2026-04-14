package com.vpm.taskserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.taskserver.dto.request.CreateTaskRequest;
import com.vpm.taskserver.dto.request.UpdateTaskRequest;
import com.vpm.taskserver.dto.template.TaskTemplate;
import com.vpm.taskserver.exception.priority.NoSuchPriorityException;
import com.vpm.taskserver.exception.task.AssignedVolunteersException;
import com.vpm.taskserver.exception.task.NoSuchTaskException;
import com.vpm.taskserver.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateTaskRequest createTaskRequest;
    private UpdateTaskRequest updateTaskRequest;
    private TaskTemplate taskTemplate;

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_ID_VALUE = "1";
    private static final String USER_ROLE_VALUE = "ADMIN";

    @BeforeEach
    void setUp() {
        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTitle("New Task");
        createTaskRequest.setDescription("Task Description");
        createTaskRequest.setDeadline(LocalDate.of(2026, 6, 1));
        createTaskRequest.setProjectId(100L);
        createTaskRequest.setPriorityId(1L);

        updateTaskRequest = new UpdateTaskRequest();
        updateTaskRequest.setTitle("Updated Task");
        updateTaskRequest.setDescription("Updated Description");
        updateTaskRequest.setDeadline(LocalDate.of(2026, 7, 1));
        updateTaskRequest.setPriorityId(1L);

        taskTemplate = TaskTemplate.builder()
                .itemId(1L)
                .title("Test Task")
                .description("Test Description")
                .priorityName("High")
                .deadline(LocalDate.of(2026, 5, 1))
                .build();
    }

    @Nested
    @DisplayName("GetTasksTests")
    class GetTasksTests {
        @Test
        @DisplayName("GET /api/tasks - Should return all tasks successfully")
        void testGetAllTasks_Success() throws Exception {
            TaskTemplate task2 = TaskTemplate.builder()
                    .itemId(2L)
                    .title("Task 2")
                    .description("Description 2")
                    .priorityName("Low")
                    .deadline(LocalDate.of(2026, 5, 15))
                    .build();

            when(taskService.getAllTasks()).thenReturn(Arrays.asList(taskTemplate, task2));

            mockMvc.perform(get("/api/tasks")
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].itemId", is(1)))
                    .andExpect(jsonPath("$[0].title", is("Test Task")))
                    .andExpect(jsonPath("$[1].itemId", is(2)))
                    .andExpect(jsonPath("$[1].title", is("Task 2")));

            verify(taskService, times(1)).getAllTasks();
        }

        @Test
        @DisplayName("GET /api/tasks - Should return empty list when no tasks exist")
        void testGetAllTasks_EmptyList() throws Exception {
            when(taskService.getAllTasks()).thenReturn(new ArrayList<>());

            mockMvc.perform(get("/api/tasks")
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(taskService, times(1)).getAllTasks();
        }

        @Test
        @DisplayName("GET /api/tasks - Should return 400 when X-User-Id header is missing")
        void testGetAllTasks_MissingUserIdHeader() throws Exception {
            mockMvc.perform(get("/api/tasks")
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/tasks - Should return 400 when X-User-Role header is missing")
        void testGetAllTasks_MissingUserRoleHeader() throws Exception {
            mockMvc.perform(get("/api/tasks")
                            .header(USER_ID_HEADER, USER_ID_VALUE))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("CreateTaskTests")
    class CreateTaskTests {
        @Test
        @DisplayName("POST /api/tasks - Should create task successfully")
        void testCreateTask_Success() throws Exception {
            doNothing().when(taskService).createTask(any(CreateTaskRequest.class));

            mockMvc.perform(post("/api/tasks")
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskRequest)))
                    .andExpect(status().isOk());

            verify(taskService, times(1)).createTask(any(CreateTaskRequest.class));
        }

        @Test
        @DisplayName("POST /api/tasks - Should return 400 when priority not found")
        void testCreateTask_PriorityNotFound() throws Exception {
            doThrow(new NoSuchPriorityException(999L))
                    .when(taskService).createTask(any(CreateTaskRequest.class));

            mockMvc.perform(post("/api/tasks")
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskRequest)))
                    .andExpect(status().isBadRequest());

            verify(taskService, times(1)).createTask(any(CreateTaskRequest.class));
        }

        @Test
        @DisplayName("POST /api/tasks - Should return 400 when headers are missing")
        void testCreateTask_MissingHeaders() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("UpdateTaskTests")
    class UpdateTaskTests {
        @Test
        @DisplayName("PUT /api/tasks/{id} - Should update task successfully")
        void testUpdateTask_Success() throws Exception {
            long taskId = 1L;
            doNothing().when(taskService).updateTask(any(UpdateTaskRequest.class), anyLong());

            mockMvc.perform(put("/api/tasks/{id}", taskId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskRequest)))
                    .andExpect(status().isOk());

            verify(taskService, times(1)).updateTask(any(UpdateTaskRequest.class), eq(taskId));
        }

        @Test
        @DisplayName("PUT /api/tasks/{id} - Should return 400 when task not found")
        void testUpdateTask_TaskNotFound() throws Exception {
            long taskId = 999L;
            doThrow(new NoSuchTaskException(taskId))
                    .when(taskService).updateTask(any(UpdateTaskRequest.class), anyLong());

            mockMvc.perform(put("/api/tasks/{id}", taskId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskRequest)))
                    .andExpect(status().isBadRequest());

            verify(taskService, times(1)).updateTask(any(UpdateTaskRequest.class), eq(taskId));
        }

        @Test
        @DisplayName("PUT /api/tasks/{id} - Should return 400 when priority not found")
        void testUpdateTask_PriorityNotFound() throws Exception {
            long taskId = 1L;
            doThrow(new NoSuchPriorityException(999L))
                    .when(taskService).updateTask(any(UpdateTaskRequest.class), anyLong());

            mockMvc.perform(put("/api/tasks/{id}", taskId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskRequest)))
                    .andExpect(status().isBadRequest());

            verify(taskService, times(1)).updateTask(any(UpdateTaskRequest.class), eq(taskId));
        }

        @Test
        @DisplayName("PUT /api/tasks/{id} - Should return 400 when headers are missing")
        void testUpdateTask_MissingHeaders() throws Exception {
            mockMvc.perform(put("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PatchTaskTests")
    class PatchTaskTests {
        @Test
        @DisplayName("PATCH /api/tasks/{id} - Should patch task successfully")
        void testPatchTask_Success() throws Exception {
            long taskId = 1L;
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", "Patched Title");
            updates.put("description", "Patched Description");

            doNothing().when(taskService).patchTask(any(), anyLong());

            mockMvc.perform(patch("/api/tasks/{id}", taskId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updates)))
                    .andExpect(status().isOk());

            verify(taskService, times(1)).patchTask(any(), eq(taskId));
        }

        @Test
        @DisplayName("PATCH /api/tasks/{id} - Should return 400 when task not found")
        void testPatchTask_TaskNotFound() throws Exception {
            long taskId = 999L;
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", "Patched Title");

            doThrow(new NoSuchTaskException(taskId))
                    .when(taskService).patchTask(any(), anyLong());

            mockMvc.perform(patch("/api/tasks/{id}", taskId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updates)))
                    .andExpect(status().isBadRequest());

            verify(taskService, times(1)).patchTask(any(), eq(taskId));
        }

        @Test
        @DisplayName("PATCH /api/tasks/{id} - Should return 400 when priority not found")
        void testPatchTask_PriorityNotFound() throws Exception {
            long taskId = 1L;
            Map<String, Object> updates = new HashMap<>();
            updates.put("priorityId", 999L);

            doThrow(new NoSuchPriorityException(999L))
                    .when(taskService).patchTask(any(), anyLong());

            mockMvc.perform(patch("/api/tasks/{id}", taskId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updates)))
                    .andExpect(status().isBadRequest());

            verify(taskService, times(1)).patchTask(any(), eq(taskId));
        }

        @Test
        @DisplayName("PATCH /api/tasks/{id} - Should return 400 when headers are missing")
        void testPatchTask_MissingHeaders() throws Exception {
            mockMvc.perform(patch("/api/tasks/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new HashMap<>())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DeleteTaskTests")
    class DeleteTaskTests {
        @Test
        @DisplayName("DELETE /api/tasks/{id} - Should delete task successfully")
        void testDeleteTask_Success() throws Exception {
            long taskId = 1L;
            doNothing().when(taskService).deleteTask(anyLong());

            mockMvc.perform(delete("/api/tasks/{id}", taskId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isOk());

            verify(taskService, times(1)).deleteTask(eq(taskId));
        }

        @Test
        @DisplayName("DELETE /api/tasks/{id} - Should return 400 when task not found")
        void testDeleteTask_TaskNotFound() throws Exception {
            long taskId = 999L;
            doThrow(new NoSuchTaskException(taskId))
                    .when(taskService).deleteTask(anyLong());

            mockMvc.perform(delete("/api/tasks/{id}", taskId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isBadRequest());

            verify(taskService, times(1)).deleteTask(eq(taskId));
        }

        @Test
        @DisplayName("DELETE /api/tasks/{id} - Should return 400 when task has assigned volunteers")
        void testDeleteTask_WithAssignedVolunteers() throws Exception {
            long taskId = 1L;
            doThrow(new AssignedVolunteersException("Volunteer Id: 1, TaskId: 1\n"))
                    .when(taskService).deleteTask(anyLong());

            mockMvc.perform(delete("/api/tasks/{id}", taskId)
                            .header(USER_ID_HEADER, USER_ID_VALUE)
                            .header(USER_ROLE_HEADER, USER_ROLE_VALUE))
                    .andExpect(status().isBadRequest());

            verify(taskService, times(1)).deleteTask(eq(taskId));
        }

        @Test
        @DisplayName("DELETE /api/tasks/{id} - Should return 400 when headers are missing")
        void testDeleteTask_MissingHeaders() throws Exception {
            mockMvc.perform(delete("/api/tasks/{id}", 1L))
                    .andExpect(status().isBadRequest());
        }
    }
}






