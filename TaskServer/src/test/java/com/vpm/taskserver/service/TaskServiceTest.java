package com.vpm.taskserver.service;

import com.vpm.taskserver.dto.request.CreateTaskRequest;
import com.vpm.taskserver.dto.request.UpdateTaskRequest;
import com.vpm.taskserver.dto.template.TaskTemplate;
import com.vpm.taskserver.entity.Priority;
import com.vpm.taskserver.entity.Task;
import com.vpm.taskserver.entity.VolunteerTask;
import com.vpm.taskserver.entity.pks.VolunteerTaskId;
import com.vpm.taskserver.exception.priority.NoSuchPriorityException;
import com.vpm.taskserver.exception.task.AssignedVolunteersException;
import com.vpm.taskserver.exception.task.NoSuchTaskException;
import com.vpm.taskserver.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PriorityService priorityService;

    @InjectMocks
    private TaskService taskService;

    private Priority testPriority;
    private Task testTask;
    private CreateTaskRequest createTaskRequest;
    private UpdateTaskRequest updateTaskRequest;

    @BeforeEach
    void setUp() {
        testPriority = Priority.builder()
                .id(1L)
                .name("High")
                .tasks(new ArrayList<>())
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .deadline(LocalDate.of(2026, 5, 1))
                .projectId(100L)
                .priority(testPriority)
                .volunteerTasks(new ArrayList<>())
                .taskSuggestions(new ArrayList<>())
                .build();

        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTitle("New Task");
        createTaskRequest.setDescription("New Description");
        createTaskRequest.setDeadline(LocalDate.of(2026, 6, 1));
        createTaskRequest.setProjectId(100L);
        createTaskRequest.setPriorityId(1L);

        updateTaskRequest = new UpdateTaskRequest();
        updateTaskRequest.setTitle("Updated Task");
        updateTaskRequest.setDescription("Updated Description");
        updateTaskRequest.setDeadline(LocalDate.of(2026, 7, 1));
        updateTaskRequest.setPriorityId(1L);
    }

    @Nested
    @DisplayName("GetTaskTests")
    class GetTaskTests {
        @Test
        @DisplayName("Should return all tasks successfully")
        void testGetAllTasks_Success() {
            Task task2 = Task.builder()
                    .id(2L)
                    .title("Task 2")
                    .description("Description 2")
                    .deadline(LocalDate.of(2026, 5, 15))
                    .projectId(100L)
                    .priority(testPriority)
                    .volunteerTasks(new ArrayList<>())
                    .taskSuggestions(new ArrayList<>())
                    .build();

            when(taskRepository.findAll()).thenReturn(Arrays.asList(testTask, task2));
            List<TaskTemplate> result = taskService.getAllTasks();

            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).getItemId());
            assertEquals(2L, result.get(1).getItemId());
            verify(taskRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no tasks exist")
        void testGetAllTasks_EmptyList() {
            when(taskRepository.findAll()).thenReturn(Collections.emptyList());
            List<TaskTemplate> result = taskService.getAllTasks();

            assertTrue(result.isEmpty());
            verify(taskRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return all project tasks successfully")
        void testGetAllProjectTasks_Success() {
            long projectId = 100L;
            when(taskRepository.getTasksByProjectId(projectId)).thenReturn(List.of(testTask));
            List<TaskTemplate> result = taskService.getAllProjectTasks(projectId);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getItemId());
            verify(taskRepository, times(1)).getTasksByProjectId(projectId);
        }

        @Test
        @DisplayName("Should return empty list when project has no tasks")
        void testGetAllProjectTasks_EmptyList() {
            long projectId = 100L;
            when(taskRepository.getTasksByProjectId(projectId)).thenReturn(Collections.emptyList());
            List<TaskTemplate> result = taskService.getAllProjectTasks(projectId);

            assertTrue(result.isEmpty());
            verify(taskRepository, times(1)).getTasksByProjectId(projectId);
        }

        @Test
        @DisplayName("Should return all volunteer tasks successfully")
        void testGetAllVolunteerTasks_Success() {
            long volunteerId = 1L;
            when(taskRepository.getTasksByVolunteerId(volunteerId)).thenReturn(List.of(testTask));
            List<TaskTemplate> result = taskService.getAllVolunteerTasks(volunteerId);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getItemId());
            verify(taskRepository, times(1)).getTasksByVolunteerId(volunteerId);
        }

        @Test
        @DisplayName("Should return empty list when volunteer has no tasks")
        void testGetAllVolunteerTasks_EmptyList() {
            long volunteerId = 1L;
            when(taskRepository.getTasksByVolunteerId(volunteerId)).thenReturn(Collections.emptyList());
            List<TaskTemplate> result = taskService.getAllVolunteerTasks(volunteerId);

            assertTrue(result.isEmpty());
            verify(taskRepository, times(1)).getTasksByVolunteerId(volunteerId);
        }

        @Test
        @DisplayName("Should return volunteer tasks in project successfully")
        void testGetAllVolunteerTasksInProject_Success() {
            long volunteerId = 1L;
            long projectId = 100L;
            when(taskRepository.getTasksInProjectByVolunteerId(projectId, volunteerId))
                    .thenReturn(List.of(testTask));
            List<TaskTemplate> result = taskService.getAllVolunteerTasksInProject(volunteerId, projectId);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getItemId());
            verify(taskRepository, times(1)).getTasksInProjectByVolunteerId(projectId, volunteerId);
        }

        @Test
        @DisplayName("Should return empty list when volunteer has no tasks in project")
        void testGetAllVolunteerTasksInProject_EmptyList() {
            long volunteerId = 1L;
            long projectId = 100L;
            when(taskRepository.getTasksInProjectByVolunteerId(projectId, volunteerId))
                    .thenReturn(Collections.emptyList());
            List<TaskTemplate> result = taskService.getAllVolunteerTasksInProject(volunteerId, projectId);

            assertTrue(result.isEmpty());
            verify(taskRepository, times(1)).getTasksInProjectByVolunteerId(projectId, volunteerId);
        }

        @Test
        @DisplayName("Should return task suggestions for volunteer in project successfully")
        void testGetTaskSuggestionsInProjectByVolunteerId_Success() {
            long volunteerId = 1L;
            long projectId = 100L;
            when(taskRepository.getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId))
                    .thenReturn(List.of(testTask));
            List<TaskTemplate> result = taskService.getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId);

            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getItemId());
            verify(taskRepository, times(1)).getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId);
        }

        @Test
        @DisplayName("Should return empty list when no task suggestions available")
        void testGetTaskSuggestionsInProjectByVolunteerId_EmptyList() {
            long volunteerId = 1L;
            long projectId = 100L;
            when(taskRepository.getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId))
                    .thenReturn(Collections.emptyList());
            List<TaskTemplate> result = taskService.getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId);

            assertTrue(result.isEmpty());
            verify(taskRepository, times(1)).getTaskSuggestionsInProjectByVolunteerId(projectId, volunteerId);
        }
    }

    @Nested
    @DisplayName("CreateTaskTests")
    class CreateTaskTests {
        @Test
        @DisplayName("Should create task successfully with valid priority")
        void testCreateTask_Success() throws NoSuchPriorityException {
            when(priorityService.getPriorityById(createTaskRequest.getPriorityId()))
                    .thenReturn(Optional.of(testPriority));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            taskService.createTask(createTaskRequest);

            verify(priorityService, times(1)).getPriorityById(createTaskRequest.getPriorityId());
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when priority not found during creation")
        void testCreateTask_PriorityNotFound() {
            when(priorityService.getPriorityById(createTaskRequest.getPriorityId()))
                    .thenReturn(Optional.empty());

            assertThrows(NoSuchPriorityException.class, () -> taskService.createTask(createTaskRequest));
            verify(priorityService, times(1)).getPriorityById(createTaskRequest.getPriorityId());
            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("UpdateTaskTests")
    class UpdateTaskTests {
        @Test
        @DisplayName("Should update task successfully with valid data")
        void testUpdateTask_Success() throws NoSuchTaskException, NoSuchPriorityException {
            long taskId = 1L;
            when(priorityService.getPriorityById(updateTaskRequest.getPriorityId()))
                    .thenReturn(Optional.of(testPriority));
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            taskService.updateTask(updateTaskRequest, taskId);

            verify(priorityService, times(1)).getPriorityById(updateTaskRequest.getPriorityId());
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when task not found during update")
        void testUpdateTask_TaskNotFound() {
            long taskId = 1L;
            when(priorityService.getPriorityById(updateTaskRequest.getPriorityId()))
                    .thenReturn(Optional.of(testPriority));
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThrows(NoSuchTaskException.class, () -> taskService.updateTask(updateTaskRequest, taskId));
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when priority not found during update")
        void testUpdateTask_PriorityNotFound() {
            long taskId = 1L;
            when(priorityService.getPriorityById(updateTaskRequest.getPriorityId()))
                    .thenReturn(Optional.empty());

            assertThrows(NoSuchPriorityException.class, () -> taskService.updateTask(updateTaskRequest, taskId));
            verify(taskRepository, never()).findById(taskId);
            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("PatchTaskTests")
    class PatchTaskTests {
        @Test
        @DisplayName("Should patch task successfully with priority update")
        void testPatchTask_Success_WithPriorityId() throws NoSuchTaskException, NoSuchPriorityException {
            long taskId = 1L;
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", "Patched Title");
            updates.put("priorityId", 1L);

            Priority newPriority = Priority.builder()
                    .id(1L)
                    .name("High")
                    .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(priorityService.getPriorityById(1L)).thenReturn(Optional.of(newPriority));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            taskService.patchTask(updates, taskId);

            verify(taskRepository, times(1)).findById(taskId);
            verify(priorityService, times(1)).getPriorityById(1L);
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("Should patch task successfully without priority update")
        void testPatchTask_Success_WithoutPriorityId() throws NoSuchTaskException, NoSuchPriorityException {
            long taskId = 1L;
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", "Patched Title");
            updates.put("description", "Patched Description");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            taskService.patchTask(updates, taskId);

            verify(taskRepository, times(1)).findById(taskId);
            verify(priorityService, never()).getPriorityById(anyLong());
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when task not found during patch")
        void testPatchTask_TaskNotFound() {
            long taskId = 1L;
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", "Patched Title");

            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThrows(NoSuchTaskException.class, () -> taskService.patchTask(updates, taskId));
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when priority not found during patch")
        void testPatchTask_PriorityNotFound() {
            long taskId = 1L;
            Map<String, Object> updates = new HashMap<>();
            updates.put("priorityId", 999L);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(priorityService.getPriorityById(999L)).thenReturn(Optional.empty());

            assertThrows(NoSuchPriorityException.class, () -> taskService.patchTask(updates, taskId));
            verify(taskRepository, times(1)).findById(taskId);
            verify(priorityService, times(1)).getPriorityById(999L);
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should handle patch with empty updates")
        void testPatchTask_EmptyUpdates() throws NoSuchTaskException, NoSuchPriorityException {
            long taskId = 1L;
            Map<String, Object> updates = new HashMap<>();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            taskService.patchTask(updates, taskId);

            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("DeleteTaskTests")
    class DeleteTaskTests {
        @Test
        @DisplayName("Should delete task successfully")
        void testDeleteTask_Success() throws NoSuchTaskException {
            long taskId = 1L;
            Task taskToDelete = Task.builder()
                    .id(taskId)
                    .title("Task to Delete")
                    .description("Description")
                    .deadline(LocalDate.of(2026, 5, 1))
                    .projectId(100L)
                    .priority(testPriority)
                    .volunteerTasks(new ArrayList<>())
                    .taskSuggestions(new ArrayList<>())
                    .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskToDelete));
            taskService.deleteTask(taskId);

            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).deleteById(taskId);
        }

        @Test
        @DisplayName("Should throw exception when task not found during deletion")
        void testDeleteTask_TaskNotFound() {
            long taskId = 1L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThrows(NoSuchTaskException.class, () -> taskService.deleteTask(taskId));
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, never()).deleteById(taskId);
        }

        @Test
        @DisplayName("Should throw exception when task has assigned volunteers")
        void testDeleteTask_WithAssignedVolunteers() {
            long taskId = 1L;
            VolunteerTaskId volunteerTaskId = new VolunteerTaskId(1L, taskId);
            VolunteerTask volunteerTask = new VolunteerTask();
            volunteerTask.setId(volunteerTaskId);

            Task taskWithVolunteers = Task.builder()
                    .id(taskId)
                    .title("Task with Volunteers")
                    .description("Description")
                    .deadline(LocalDate.of(2026, 5, 1))
                    .projectId(100L)
                    .priority(testPriority)
                    .volunteerTasks(new ArrayList<>(List.of(volunteerTask)))
                    .taskSuggestions(new ArrayList<>())
                    .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskWithVolunteers));

            assertThrows(AssignedVolunteersException.class, () -> taskService.deleteTask(taskId));
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, never()).deleteById(taskId);
        }

        @Test
        @DisplayName("Should throw exception when task has multiple assigned volunteers")
        void testDeleteTask_WithMultipleAssignedVolunteers() {
            long taskId = 1L;
            VolunteerTaskId volunteerTaskId1 = new VolunteerTaskId(1L, taskId);
            VolunteerTaskId volunteerTaskId2 = new VolunteerTaskId(2L, taskId);

            VolunteerTask volunteerTask1 = new VolunteerTask();
            volunteerTask1.setId(volunteerTaskId1);

            VolunteerTask volunteerTask2 = new VolunteerTask();
            volunteerTask2.setId(volunteerTaskId2);

            Task taskWithMultipleVolunteers = Task.builder()
                    .id(taskId)
                    .title("Task with Multiple Volunteers")
                    .description("Description")
                    .deadline(LocalDate.of(2026, 5, 1))
                    .projectId(100L)
                    .priority(testPriority)
                    .volunteerTasks(new ArrayList<>(List.of(volunteerTask1, volunteerTask2)))
                    .taskSuggestions(new ArrayList<>())
                    .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskWithMultipleVolunteers));

            assertThrows(AssignedVolunteersException.class, () -> taskService.deleteTask(taskId));
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, never()).deleteById(taskId);
        }
    }

    @Nested
    @DisplayName("IntegrationTests")
    class IntegrationTests {
        @Test
        @DisplayName("Should create and update task in sequence")
        void testCreateTaskAndUpdateTask_IntegrationScenario() throws NoSuchTaskException, NoSuchPriorityException {
            when(priorityService.getPriorityById(1L)).thenReturn(Optional.of(testPriority));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

            taskService.createTask(createTaskRequest);
            taskService.updateTask(updateTaskRequest, 1L);

            verify(priorityService, times(2)).getPriorityById(1L);
            verify(taskRepository, times(2)).save(any(Task.class));
        }

        @Test
        @DisplayName("Should patch task with multiple fields and priority update")
        void testPatchTask_MultipleFieldsWithPriority() throws NoSuchTaskException, NoSuchPriorityException {
            long taskId = 1L;
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", "New Title");
            updates.put("description", "New Description");
            updates.put("priorityId", 2L);

            Priority newPriority = Priority.builder()
                    .id(2L)
                    .name("Low")
                    .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
            when(priorityService.getPriorityById(2L)).thenReturn(Optional.of(newPriority));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            taskService.patchTask(updates, taskId);

            verify(taskRepository, times(1)).findById(taskId);
            verify(priorityService, times(1)).getPriorityById(2L);
            verify(taskRepository, times(1)).save(any(Task.class));
        }
    }

}

