package com.vpm.taskserver.integration.repository;

import com.vpm.taskserver.config.IntegrationTestsDBConfig;
import com.vpm.taskserver.entity.Priority;
import com.vpm.taskserver.entity.Task;
import com.vpm.taskserver.repository.PriorityRepository;
import com.vpm.taskserver.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(IntegrationTestsDBConfig.class)
@DisplayName("TaskRepository Integration Tests")
@Transactional
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    @Autowired
    private EntityManager entityManager;

    private Priority highPriority;
    private Priority lowPriority;
    private Task task1;
    private Task task2;

    private static final long PROJECT_ID_1 = 1L;
    private static final long PROJECT_ID_2 = 2L;
    private static final long PROJECT_ID_3 = 3L;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        taskRepository.deleteAll();
        priorityRepository.deleteAll();

        // Create priorities
        highPriority = Priority.builder().name("High").build();
        highPriority = priorityRepository.save(highPriority);

        lowPriority = Priority.builder().name("Low").build();
        lowPriority = priorityRepository.save(lowPriority);

        // Create tasks for project 1
        task1 = Task.builder()
                .title("Task 1 - Project 1")
                .description("Description 1")
                .deadline(LocalDate.of(2026, 5, 1))
                .projectId(PROJECT_ID_1)
                .priority(highPriority)
                .build();
        task1 = taskRepository.save(task1);

        task2 = Task.builder()
                .title("Task 2 - Project 1")
                .description("Description 2")
                .deadline(LocalDate.of(2026, 6, 1))
                .projectId(PROJECT_ID_1)
                .priority(lowPriority)
                .build();
        task2 = taskRepository.save(task2);

        // Create task for project 2
        Task task3 = Task.builder()
                .title("Task 3 - Project 2")
                .description("Description 3")
                .deadline(LocalDate.of(2026, 7, 1))
                .projectId(PROJECT_ID_2)
                .priority(highPriority)
                .build();
        taskRepository.save(task3);
    }

    @Nested
    @DisplayName("getTaskById Tests")
    class GetTaskByIdTests {

        @Test
        @DisplayName("Should retrieve task by ID successfully")
        void testGetTaskById_Success() {
            // Act
            Optional<Task> result = taskRepository.getTaskById(task1.getId());

            // Assert
            assertTrue(result.isPresent());
            Task found = result.get();
            assertEquals("Task 1 - Project 1", found.getTitle());
            assertEquals("Description 1", found.getDescription());
            assertNotNull(found.getPriority());
            assertEquals("High", found.getPriority().getName());
        }

        @Test
        @DisplayName("Should return empty optional when task not found")
        void testGetTaskById_NotFound() {
            // Act
            Optional<Task> result = taskRepository.getTaskById(999L);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should fetch priority with task")
        void testGetTaskById_WithPriority() {
            // Act
            Optional<Task> result = taskRepository.getTaskById(task2.getId());

            // Assert
            assertTrue(result.isPresent());
            Task found = result.get();
            assertNotNull(found.getPriority());
            assertEquals("Low", found.getPriority().getName());
        }

        @Test
        @DisplayName("Should have correct deadline when retrieved")
        void testGetTaskById_WithDeadline() {
            // Act
            Optional<Task> result = taskRepository.getTaskById(task1.getId());

            // Assert
            assertTrue(result.isPresent());
            assertEquals(LocalDate.of(2026, 5, 1), result.get().getDeadline());
        }
    }

    @Nested
    @DisplayName("getAllTasksWithPriority Tests")
    class GetAllTasksWithPriorityTests {

        @Test
        @DisplayName("Should retrieve all tasks with priority")
        void testGetAllTasksWithPriority_Success() {
            // Act
            List<Task> result = taskRepository.getAllTasksWithPriority();

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());
            result.forEach(task -> assertNotNull(task.getPriority()));
        }

        @Test
        @DisplayName("Should return empty list when no tasks exist")
        void testGetAllTasksWithPriority_Empty() {
            // Arrange
            taskRepository.deleteAll();

            // Act
            List<Task> result = taskRepository.getAllTasksWithPriority();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should retrieve tasks with different priorities")
        void testGetAllTasksWithPriority_DifferentPriorities() {
            // Act
            List<Task> result = taskRepository.getAllTasksWithPriority();

            // Assert
            assertEquals(3, result.size());
            assertTrue(result.stream().anyMatch(t -> t.getPriority().getName().equals("High")));
            assertTrue(result.stream().anyMatch(t -> t.getPriority().getName().equals("Low")));
        }

        @Test
        @DisplayName("Should include all project tasks")
        void testGetAllTasksWithPriority_AllProjects() {
            // Act
            List<Task> result = taskRepository.getAllTasksWithPriority();

            // Assert
            assertTrue(result.stream().anyMatch(t -> t.getProjectId() == PROJECT_ID_1));
            assertTrue(result.stream().anyMatch(t -> t.getProjectId() == PROJECT_ID_2));
        }
    }

    @Nested
    @DisplayName("getTasksByProjectId Tests")
    class GetTasksByProjectIdTests {

        @Test
        @DisplayName("Should retrieve all tasks for a project")
        void testGetTasksByProjectId_Success() {
            // Act
            List<Task> result = taskRepository.getTasksByProjectId(PROJECT_ID_1);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(t -> t.getProjectId() == PROJECT_ID_1));
        }

        @Test
        @DisplayName("Should return empty list for project with no tasks")
        void testGetTasksByProjectId_NoTasks() {
            // Act
            List<Task> result = taskRepository.getTasksByProjectId(PROJECT_ID_3);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should fetch priority with project tasks")
        void testGetTasksByProjectId_WithPriority() {
            // Act
            List<Task> result = taskRepository.getTasksByProjectId(PROJECT_ID_1);

            // Assert
            assertEquals(2, result.size());
            result.forEach(task -> assertNotNull(task.getPriority()));
        }

        @Test
        @DisplayName("Should retrieve tasks from different projects independently")
        void testGetTasksByProjectId_MultipleProjects() {
            // Act
            List<Task> project1Tasks = taskRepository.getTasksByProjectId(PROJECT_ID_1);
            List<Task> project2Tasks = taskRepository.getTasksByProjectId(PROJECT_ID_2);

            // Assert
            assertEquals(2, project1Tasks.size());
            assertEquals(1, project2Tasks.size());
            assertTrue(project1Tasks.stream().allMatch(t -> t.getProjectId() == PROJECT_ID_1));
            assertTrue(project2Tasks.stream().allMatch(t -> t.getProjectId() == PROJECT_ID_2));
        }

        @Test
        @DisplayName("Should contain correct task titles per project")
        void testGetTasksByProjectId_VerifyTitles() {
            // Act
            List<Task> project1Tasks = taskRepository.getTasksByProjectId(PROJECT_ID_1);
            List<Task> project2Tasks = taskRepository.getTasksByProjectId(PROJECT_ID_2);

            // Assert
            assertTrue(project1Tasks.stream().anyMatch(t -> t.getTitle().equals("Task 1 - Project 1")));
            assertTrue(project1Tasks.stream().anyMatch(t -> t.getTitle().equals("Task 2 - Project 1")));
            assertTrue(project2Tasks.stream().anyMatch(t -> t.getTitle().equals("Task 3 - Project 2")));
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CRUDOperations {

        @Test
        @DisplayName("Should save new task successfully")
        void testSave_Success() {
            // Arrange
            Task newTask = Task.builder()
                    .title("New Task")
                    .description("New Description")
                    .deadline(LocalDate.of(2026, 8, 1))
                    .projectId(PROJECT_ID_3)
                    .priority(lowPriority)
                    .build();

            // Act
            Task saved = taskRepository.save(newTask);

            // Assert
            assertTrue(taskRepository.existsById(saved.getId()));
        }

        @Test
        @DisplayName("Should update task successfully")
        @Transactional
        void testUpdate_Success() {
            // Arrange
            long taskId = task1.getId();
            String originalTitle = task1.getTitle();
            task1.setTitle("Updated Title");
            task1.setDescription("Updated Description");

            // Save the task
            taskRepository.save(task1);
            // Flush to ensure changes are written to database
            entityManager.flush();
            // Clear session to ensure fresh fetch from database
            entityManager.clear();

            // Assert - Use the custom query to get fresh data
            Task retrieved = taskRepository.getTaskById(taskId).orElseThrow(() ->
                new RuntimeException("Task with id " + taskId + " not found after update"));
            assertEquals("Updated Title", retrieved.getTitle());
            assertEquals("Updated Description", retrieved.getDescription());
        }

        @Test
        @DisplayName("Should delete task successfully")
        void testDelete_Success() {
            // Arrange
            long taskId = task1.getId();

            // Act
            taskRepository.deleteById(taskId);

            // Assert
            assertFalse(taskRepository.existsById(taskId));
        }

        @Test
        @DisplayName("Should find all tasks")
        void testFindAll_Success() {
            // Act
            List<Task> result = taskRepository.findAll();

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Should check task existence")
        void testExists_Success() {
            // Assert
            assertTrue(taskRepository.existsById(task1.getId()));
            assertFalse(taskRepository.existsById(999L));
        }

        @Test
        @DisplayName("Should count tasks")
        void testCount_Success() {
            // Act
            long count = taskRepository.count();

            // Assert
            assertEquals(3L, count);
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should maintain task data integrity")
        void testDataIntegrity_TaskFields() {
            // Act
            Optional<Task> result = taskRepository.getTaskById(task1.getId());

            // Assert
            assertTrue(result.isPresent());
            Task task = result.get();
            assertEquals(task1.getTitle(), task.getTitle());
            assertEquals(task1.getDescription(), task.getDescription());
            assertEquals(task1.getDeadline(), task.getDeadline());
            assertEquals(task1.getProjectId(), task.getProjectId());
        }

        @Test
        @DisplayName("Should maintain relationship with priority")
        void testDataIntegrity_PriorityRelationship() {
            // Act
            Optional<Task> result = taskRepository.getTaskById(task1.getId());

            // Assert
            assertTrue(result.isPresent());
            Task task = result.get();
            assertNotNull(task.getPriority());
            assertEquals(highPriority.getId(), task.getPriority().getId());
        }

        @Test
        @DisplayName("Should maintain consistent task count")
        void testDataIntegrity_TaskCount() {
            // Arrange
            long initialCount = taskRepository.count();

            // Act
            Task newTask = Task.builder()
                    .title("Integrity Test Task")
                    .description("Description")
                    .deadline(LocalDate.of(2026, 9, 1))
                    .projectId(PROJECT_ID_1)
                    .priority(lowPriority)
                    .build();
            taskRepository.save(newTask);

            // Assert
            assertEquals(initialCount + 1, taskRepository.count());
        }

        @Test
        @DisplayName("Should preserve deadline precision")
        void testDataIntegrity_Deadline() {
            // Arrange
            LocalDate expectedDeadline = LocalDate.of(2026, 12, 31);
            Task testTask = Task.builder()
                    .title("Deadline Test")
                    .description("Description")
                    .deadline(expectedDeadline)
                    .projectId(PROJECT_ID_1)
                    .priority(highPriority)
                    .build();
            Task saved = taskRepository.save(testTask);

            // Act
            Optional<Task> retrieved = taskRepository.getTaskById(saved.getId());

            // Assert
            assertTrue(retrieved.isPresent());
            assertEquals(expectedDeadline, retrieved.get().getDeadline());
        }
    }

    @Nested
    @DisplayName("Query Performance Tests")
    class QueryPerformanceTests {

        @Test
        @DisplayName("Should efficiently retrieve tasks by project")
        void testQueryPerformance_ProjectTasks() {
            // Act
            List<Task> result = taskRepository.getTasksByProjectId(PROJECT_ID_1);

            // Assert
            assertEquals(2, result.size());
            result.forEach(task -> assertNotNull(task.getPriority())); // Verify eager loading
        }

        @Test
        @DisplayName("Should efficiently retrieve all tasks with priorities")
        void testQueryPerformance_AllTasks() {
            // Act
            List<Task> result = taskRepository.getAllTasksWithPriority();

            // Assert
            assertEquals(3, result.size());
            result.forEach(task -> assertNotNull(task.getPriority())); // Verify eager loading
        }

        @Test
        @DisplayName("Should efficiently retrieve single task with priority")
        void testQueryPerformance_SingleTask() {
            // Act
            Optional<Task> result = taskRepository.getTaskById(task1.getId());

            // Assert
            assertTrue(result.isPresent());
            assertNotNull(result.get().getPriority()); // Verify eager loading
        }
    }
}




