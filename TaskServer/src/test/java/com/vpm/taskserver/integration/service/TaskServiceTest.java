package com.vpm.taskserver.integration.service;

import com.vpm.taskserver.config.IntegrationTestsDBConfig;
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
import com.vpm.taskserver.repository.PriorityRepository;
import com.vpm.taskserver.repository.TaskRepository;
import com.vpm.taskserver.repository.VolunteerTaskRepository;
import com.vpm.taskserver.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(IntegrationTestsDBConfig.class)
@DisplayName("TaskService Integration Tests")
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    @Autowired
    private VolunteerTaskRepository volunteerTaskRepository;

    private Priority testPriority;
    private Priority lowPriority;
    private Task testTask;

    private static final long TEST_PROJECT_ID = 1L;
    private static final long TEST_VOLUNTEER_ID = 100L;
    private static final long SECOND_VOLUNTEER_ID = 101L;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        volunteerTaskRepository.deleteAll();
        taskRepository.deleteAll();
        priorityRepository.deleteAll();

        // Create test priorities
        testPriority = Priority.builder()
                .name("High")
                .build();
        testPriority = priorityRepository.save(testPriority);

        lowPriority = Priority.builder()
                .name("Low")
                .build();
        lowPriority = priorityRepository.save(lowPriority);

        // Create test task
        testTask = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .deadline(LocalDate.of(2026, 5, 1))
                .projectId(TEST_PROJECT_ID)
                .priority(testPriority)
                .build();
        testTask = taskRepository.save(testTask);
    }

    @Nested
    @DisplayName("GET Operations")
    class GetOperations {

        @Test
        @DisplayName("Should retrieve all tasks successfully")
        void testGetAllTasks() {
            // Arrange
            Task task2 = Task.builder()
                    .title("Second Task")
                    .description("Second Description")
                    .deadline(LocalDate.of(2026, 6, 1))
                    .projectId(TEST_PROJECT_ID)
                    .priority(lowPriority)
                    .build();
            taskRepository.save(task2);

            // Act
            List<TaskTemplate> result = taskService.getAllTasks();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(t -> t.getTitle().equals("Test Task")));
            assertTrue(result.stream().anyMatch(t -> t.getTitle().equals("Second Task")));
        }

        @Test
        @DisplayName("Should return empty list when no tasks exist")
        void testGetAllTasks_Empty() {
            // Arrange
            taskRepository.deleteAll();

            // Act
            List<TaskTemplate> result = taskService.getAllTasks();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should retrieve all tasks for a project")
        void testGetAllProjectTasks_Success() {
            // Arrange
            Task task2 = Task.builder()
                    .title("Another Project Task")
                    .description("Description")
                    .deadline(LocalDate.of(2026, 6, 1))
                    .projectId(TEST_PROJECT_ID)
                    .priority(lowPriority)
                    .build();
            taskRepository.save(task2);

            // Act
            List<TaskTemplate> result = taskService.getAllProjectTasks(TEST_PROJECT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return empty list when project has no tasks")
        void testGetAllProjectTasks_NoTasks() {
            // Act
            List<TaskTemplate> result = taskService.getAllProjectTasks(999L);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should retrieve all tasks for a volunteer")
        void testGetAllVolunteerTasks_Success() {
            // Arrange
            VolunteerTaskId volunteerTaskId = new VolunteerTaskId(TEST_VOLUNTEER_ID, testTask.getId());
            VolunteerTask volunteerTask = VolunteerTask.builder()
                    .id(volunteerTaskId)
                    .task(testTask)
                    .build();
            volunteerTaskRepository.save(volunteerTask);

            // Act
            List<TaskTemplate> result = taskService.getAllVolunteerTasks(TEST_VOLUNTEER_ID);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Test Task", result.get(0).getTitle());
        }

        @Test
        @DisplayName("Should return empty list when volunteer has no tasks")
        void testGetAllVolunteerTasks_NoTasks() {
            // Act
            List<TaskTemplate> result = taskService.getAllVolunteerTasks(999L);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should retrieve volunteer tasks in a specific project")
        void testGetAllVolunteerTasksInProject_Success() {
            // Arrange
            VolunteerTaskId volunteerTaskId = new VolunteerTaskId(TEST_VOLUNTEER_ID, testTask.getId());
            VolunteerTask volunteerTask = VolunteerTask.builder()
                    .id(volunteerTaskId)
                    .task(testTask)
                    .build();
            volunteerTaskRepository.save(volunteerTask);

            // Act
            List<TaskTemplate> result = taskService.getAllVolunteerTasksInProject(TEST_VOLUNTEER_ID, TEST_PROJECT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Test Task", result.get(0).getTitle());
        }

        @Test
        @DisplayName("Should return empty list when volunteer has no tasks in project")
        void testGetAllVolunteerTasksInProject_NoTasks() {
            // Act
            List<TaskTemplate> result = taskService.getAllVolunteerTasksInProject(TEST_VOLUNTEER_ID, TEST_PROJECT_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("CREATE Operations")
    class CreateOperations {

        @Test
        @DisplayName("Should create task successfully with valid priority")
        void testCreateTask_Success() throws NoSuchPriorityException {
            // Arrange
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("New Task");
            request.setDescription("New Description");
            request.setDeadline(LocalDate.of(2026, 7, 1));
            request.setProjectId(2L);
            request.setPriorityId(testPriority.getId());

            int initialCount = (int) taskRepository.count();

            // Act
            TaskTemplate result = taskService.createTask(request);

            // Assert
            assertNotNull(result);
            assertEquals("New Task", result.getTitle());
            assertEquals("New Description", result.getDescription());
            assertEquals(initialCount + 1, taskRepository.count());
        }

        @Test
        @DisplayName("Should throw exception when priority not found")
        void testCreateTask_PriorityNotFound() {
            // Arrange
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("New Task");
            request.setDescription("Description");
            request.setDeadline(LocalDate.of(2026, 7, 1));
            request.setProjectId(2L);
            request.setPriorityId(999L);

            // Act & Assert
            assertThrows(NoSuchPriorityException.class, () -> taskService.createTask(request));
        }

        @Test
        @DisplayName("Should create task with correct deadline")
        void testCreateTask_VerifyDeadline() throws NoSuchPriorityException {
            // Arrange
            LocalDate deadline = LocalDate.of(2026, 8, 15);
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Deadline Task");
            request.setDescription("Description");
            request.setDeadline(deadline);
            request.setProjectId(TEST_PROJECT_ID);
            request.setPriorityId(testPriority.getId());

            // Act
            TaskTemplate result = taskService.createTask(request);

            // Assert
            assertNotNull(result);
            assertEquals(deadline, result.getDeadline());
        }
    }

    @Nested
    @DisplayName("UPDATE Operations")
    class UpdateOperations {

        @Test
        @DisplayName("Should update task successfully")
        void testUpdateTask_Success() throws NoSuchTaskException, NoSuchPriorityException {
            // Arrange
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("Updated Title");
            request.setDescription("Updated Description");
            request.setDeadline(LocalDate.of(2026, 9, 1));
            request.setPriorityId(lowPriority.getId());

            // Act
            TaskTemplate result = taskService.updateTask(request, testTask.getId());

            // Assert
            assertNotNull(result);
            assertEquals("Updated Title", result.getTitle());
            assertEquals("Updated Description", result.getDescription());
            assertEquals(LocalDate.of(2026, 9, 1), result.getDeadline());
            assertEquals("Low", result.getPriorityName());

            // Verify in database
            Task updatedTask = taskRepository.getTaskById(testTask.getId()).orElseThrow();
            assertEquals("Updated Title", updatedTask.getTitle());
        }

        @Test
        @DisplayName("Should throw exception when task not found")
        void testUpdateTask_TaskNotFound() {
            // Arrange
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("Updated Title");
            request.setDescription("Description");
            request.setDeadline(LocalDate.of(2026, 9, 1));
            request.setPriorityId(testPriority.getId());

            // Act & Assert
            assertThrows(NoSuchTaskException.class, () -> taskService.updateTask(request, 999L));
        }

        @Test
        @DisplayName("Should throw exception when priority not found during update")
        void testUpdateTask_PriorityNotFound() {
            // Arrange
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("Updated Title");
            request.setDescription("Description");
            request.setDeadline(LocalDate.of(2026, 9, 1));
            request.setPriorityId(999L);

            // Act & Assert
            assertThrows(NoSuchPriorityException.class, () -> taskService.updateTask(request, testTask.getId()));
        }
    }

    @Nested
    @DisplayName("PATCH Operations")
    class PatchOperations {

        @Test
        @DisplayName("Should patch task with title only")
        void testPatchTask_TitleOnly() throws NoSuchTaskException, NoSuchPriorityException {
            // Arrange
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", "Patched Title");

            // Act
            TaskTemplate result = taskService.patchTask(updates, testTask.getId());

            // Assert
            assertNotNull(result);
            assertEquals("Patched Title", result.getTitle());
            assertEquals("Test Description", result.getDescription()); // Should remain unchanged

            // Verify in database
            Task patchedTask = taskRepository.getTaskById(testTask.getId()).orElseThrow();
            assertEquals("Patched Title", patchedTask.getTitle());
        }

        @Test
        @DisplayName("Should patch task with description and priority")
        void testPatchTask_DescriptionAndPriority() throws NoSuchTaskException, NoSuchPriorityException {
            // Arrange
            Map<String, Object> updates = new HashMap<>();
            updates.put("description", "New Description");
            updates.put("priorityId", lowPriority.getId());

            // Act
            TaskTemplate result = taskService.patchTask(updates, testTask.getId());

            // Assert
            assertNotNull(result);
            assertEquals("New Description", result.getDescription());
            assertEquals("Low", result.getPriorityName());
            assertEquals("Test Task", result.getTitle()); // Should remain unchanged
        }

        @Test
        @DisplayName("Should patch task with deadline")
        void testPatchTask_Deadline() throws NoSuchTaskException, NoSuchPriorityException {
            // Arrange
            LocalDate newDeadline = LocalDate.of(2026, 10, 1);
            Map<String, Object> updates = new HashMap<>();
            updates.put("deadline", newDeadline);

            // Act
            TaskTemplate result = taskService.patchTask(updates, testTask.getId());

            // Assert
            assertNotNull(result);
            assertEquals(newDeadline, result.getDeadline());
        }

        @Test
        @DisplayName("Should patch task with empty updates")
        void testPatchTask_EmptyUpdates() throws NoSuchTaskException, NoSuchPriorityException {
            // Arrange
            Map<String, Object> updates = new HashMap<>();

            // Act
            TaskTemplate result = taskService.patchTask(updates, testTask.getId());

            // Assert
            assertNotNull(result);
            assertEquals("Test Task", result.getTitle());
            assertEquals("Test Description", result.getDescription());
        }

        @Test
        @DisplayName("Should throw exception when task not found during patch")
        void testPatchTask_TaskNotFound() {
            // Arrange
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", "New Title");

            // Act & Assert
            assertThrows(NoSuchTaskException.class, () -> taskService.patchTask(updates, 999L));
        }

        @Test
        @DisplayName("Should throw exception when priority not found during patch")
        void testPatchTask_PriorityNotFound() {
            // Arrange
            Map<String, Object> updates = new HashMap<>();
            updates.put("priorityId", 999L);

            // Act & Assert
            assertThrows(NoSuchPriorityException.class, () -> taskService.patchTask(updates, testTask.getId()));
        }

        @Test
        @DisplayName("Should patch multiple fields simultaneously")
        void testPatchTask_MultipleFields() throws NoSuchTaskException, NoSuchPriorityException {
            // Arrange
            LocalDate newDeadline = LocalDate.of(2026, 11, 15);
            Map<String, Object> updates = new HashMap<>();
            updates.put("title", "Multi Patched");
            updates.put("description", "Multi Description");
            updates.put("deadline", newDeadline);
            updates.put("priorityId", lowPriority.getId());

            // Act
            TaskTemplate result = taskService.patchTask(updates, testTask.getId());

            // Assert
            assertNotNull(result);
            assertEquals("Multi Patched", result.getTitle());
            assertEquals("Multi Description", result.getDescription());
            assertEquals(newDeadline, result.getDeadline());
            assertEquals("Low", result.getPriorityName());
        }
    }

    @Nested
    @DisplayName("DELETE Operations")
    class DeleteOperations {

        @Test
        @DisplayName("Should delete task successfully when no volunteers assigned")
        void testDeleteTask_Success() throws NoSuchTaskException {
            // Arrange
            long taskId = testTask.getId();
            assertTrue(taskRepository.existsById(taskId));

            // Act
            taskService.deleteTask(taskId);

            // Assert
            assertFalse(taskRepository.existsById(taskId));
        }

        @Test
        @DisplayName("Should throw exception when task not found")
        void testDeleteTask_TaskNotFound() {
            // Act & Assert
            assertThrows(NoSuchTaskException.class, () -> taskService.deleteTask(999L));
        }

        @Test
        @DisplayName("Should throw exception when task has assigned volunteers")
        void testDeleteTask_WithAssignedVolunteer() {
            // Arrange
            VolunteerTaskId volunteerTaskId = new VolunteerTaskId(TEST_VOLUNTEER_ID, testTask.getId());
            VolunteerTask volunteerTask = VolunteerTask.builder()
                    .id(volunteerTaskId)
                    .task(testTask)
                    .build();
            volunteerTaskRepository.save(volunteerTask);

            // Act & Assert
            assertThrows(AssignedVolunteersException.class, () -> taskService.deleteTask(testTask.getId()));

            // Verify task still exists in database
            assertTrue(taskRepository.existsById(testTask.getId()));
        }

        @Test
        @DisplayName("Should throw exception when task has multiple assigned volunteers")
        void testDeleteTask_WithMultipleAssignedVolunteers() {
            // Arrange
            VolunteerTaskId volunteerTaskId1 = new VolunteerTaskId(TEST_VOLUNTEER_ID, testTask.getId());
            VolunteerTask volunteerTask1 = VolunteerTask.builder()
                    .id(volunteerTaskId1)
                    .task(testTask)
                    .build();
            volunteerTaskRepository.save(volunteerTask1);

            VolunteerTaskId volunteerTaskId2 = new VolunteerTaskId(SECOND_VOLUNTEER_ID, testTask.getId());
            VolunteerTask volunteerTask2 = VolunteerTask.builder()
                    .id(volunteerTaskId2)
                    .task(testTask)
                    .build();
            volunteerTaskRepository.save(volunteerTask2);

            // Act & Assert
            AssignedVolunteersException exception = assertThrows(
                    AssignedVolunteersException.class,
                    () -> taskService.deleteTask(testTask.getId())
            );

            // Verify exception message contains volunteer info
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("Volunteer Id"));

            // Verify task still exists
            assertTrue(taskRepository.existsById(testTask.getId()));
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Should create, update, and patch task in sequence")
        void testCreateUpdatePatchSequence() throws NoSuchPriorityException, NoSuchTaskException {
            // Create
            CreateTaskRequest createRequest = new CreateTaskRequest();
            createRequest.setTitle("Scenario Task");
            createRequest.setDescription("Initial Description");
            createRequest.setDeadline(LocalDate.of(2026, 5, 1));
            createRequest.setProjectId(TEST_PROJECT_ID);
            createRequest.setPriorityId(testPriority.getId());

            TaskTemplate created = taskService.createTask(createRequest);
            assertNotNull(created);
            long createdTaskId = created.getItemId();

            // Update
            UpdateTaskRequest updateRequest = new UpdateTaskRequest();
            updateRequest.setTitle("Updated Scenario Task");
            updateRequest.setDescription("Updated Description");
            updateRequest.setDeadline(LocalDate.of(2026, 6, 1));
            updateRequest.setPriorityId(lowPriority.getId());

            TaskTemplate updated = taskService.updateTask(updateRequest, createdTaskId);
            assertEquals("Updated Scenario Task", updated.getTitle());

            // Patch
            Map<String, Object> patchUpdates = new HashMap<>();
            patchUpdates.put("title", "Patched Scenario Task");

            TaskTemplate patched = taskService.patchTask(patchUpdates, createdTaskId);
            assertEquals("Patched Scenario Task", patched.getTitle());
            assertEquals("Updated Description", patched.getDescription());
        }

        @Test
        @DisplayName("Should handle multiple tasks in same project")
        void testMultipleTasksInProject() throws NoSuchPriorityException {
            // Arrange
            CreateTaskRequest request1 = new CreateTaskRequest();
            request1.setTitle("Task 1");
            request1.setDescription("Description 1");
            request1.setDeadline(LocalDate.of(2026, 5, 1));
            request1.setProjectId(TEST_PROJECT_ID);
            request1.setPriorityId(testPriority.getId());

            CreateTaskRequest request2 = new CreateTaskRequest();
            request2.setTitle("Task 2");
            request2.setDescription("Description 2");
            request2.setDeadline(LocalDate.of(2026, 6, 1));
            request2.setProjectId(TEST_PROJECT_ID);
            request2.setPriorityId(lowPriority.getId());

            // Act
            taskService.createTask(request1);
            taskService.createTask(request2);

            List<TaskTemplate> projectTasks = taskService.getAllProjectTasks(TEST_PROJECT_ID);

            // Assert
            assertNotNull(projectTasks);
            assertEquals(3, projectTasks.size()); // testTask + 2 new tasks
            assertTrue(projectTasks.stream().anyMatch(t -> t.getTitle().equals("Task 1")));
            assertTrue(projectTasks.stream().anyMatch(t -> t.getTitle().equals("Task 2")));
        }

        @Test
        @DisplayName("Should verify task priority relationship")
        void testTaskPriorityRelationship() throws NoSuchPriorityException {
            // Arrange
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("Priority Task");
            request.setDescription("Description");
            request.setDeadline(LocalDate.of(2026, 5, 1));
            request.setProjectId(TEST_PROJECT_ID);
            request.setPriorityId(lowPriority.getId());

            // Act
            TaskTemplate result = taskService.createTask(request);

            // Assert
            assertEquals("Low", result.getPriorityName());

            // Verify in database
            Task savedTask = taskRepository.getTaskById(result.getItemId()).orElseThrow();
            assertEquals(lowPriority.getId(), savedTask.getPriority().getId());
        }
    }
}

