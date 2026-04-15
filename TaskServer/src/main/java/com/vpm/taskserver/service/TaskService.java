package com.vpm.taskserver.service;

import com.vpm.taskserver.dto.request.CreateTaskRequest;
import com.vpm.taskserver.dto.request.UpdateTaskRequest;
import com.vpm.taskserver.dto.template.TaskTemplate;
import com.vpm.taskserver.entity.Priority;
import com.vpm.taskserver.entity.Task;
import com.vpm.taskserver.entity.VolunteerTask;
import com.vpm.taskserver.entity.mapper.TaskMapper;
import com.vpm.taskserver.exception.priority.NoSuchPriorityException;
import com.vpm.taskserver.exception.task.AssignedVolunteersException;
import com.vpm.taskserver.exception.task.NoSuchTaskException;
import com.vpm.taskserver.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final PriorityService priorityService;

    @Autowired
    public TaskService(
            TaskRepository taskRepository,
            PriorityService priorityService) {
        this.taskRepository = taskRepository;
        this.priorityService = priorityService;
    }

    /*
     * GET HTTP method
     */

    public List<TaskTemplate> getAllTasks() {

        return taskRepository.getAllTasksWithPriority().stream()
                .map(TaskMapper::toTaskTemplate)
                .toList();

    }

    public List<TaskTemplate> getAllProjectTasks(
            long projectId
    ) {
        List<TaskTemplate> projectTasks = taskRepository.getTasksByProjectId(projectId).stream()
                .map(TaskMapper::toTaskTemplate)
                .toList();

        if (projectTasks.isEmpty()) {
            log.warn("No tasks found for project id {}", projectId);
        }
        return projectTasks;
    }

    public List<TaskTemplate> getAllVolunteerTasks(
            long volunteerId
    ) {
        List<TaskTemplate> volunteerTasks = taskRepository.getTasksByVolunteerId(volunteerId).stream()
                .map(TaskMapper::toTaskTemplate)
                .toList();

        if (volunteerTasks.isEmpty()) {
            log.warn("No tasks found for volunteer id {}", volunteerId);
        }

        return volunteerTasks;
    }

    public List<TaskTemplate> getAllVolunteerTasksInProject(
            long volunteerId,
            long projectId
    ) {
        List<TaskTemplate> volunteerTasksInProject = taskRepository.getTasksInProjectByVolunteerId(
                projectId,
                volunteerId
        ).stream()
                .map(TaskMapper::toTaskTemplate)
                .toList();

        if (volunteerTasksInProject.isEmpty()) {
            log.warn("No tasks found for volunteer id {} in project of id {}", volunteerId, projectId);
        }

        return volunteerTasksInProject;
    }

    public List<TaskTemplate> getTaskSuggestionsInProjectByVolunteerId(
            long projectId,
            long volunteerId
    ) {
        List<TaskTemplate> volunteerTaskSuggestions = taskRepository.getTaskSuggestionsInProjectByVolunteerId(
                projectId,
                volunteerId
        ).stream()
                .map(TaskMapper::toTaskTemplate)
                .toList();

        if (volunteerTaskSuggestions.isEmpty()) {
            log.warn("No task suggestions found for volunteer id {} in project of id {}", volunteerId, projectId);
        }

        return volunteerTaskSuggestions;
    }

    /*
     * POST HTTP method
     */

    // TODO: replace Optional<Object> with Object and .orElseThrow with Optional API in every service and module!

    public TaskTemplate createTask(
            CreateTaskRequest request
    ) throws NoSuchPriorityException {

        Priority priority = getPriorityById(
                request.getPriorityId()
        );

        Task task = TaskMapper.fromCreateTaskRequest(
                request,
                priority
        );

        return TaskMapper
                .toTaskTemplate(
                        taskRepository.save(task)
                );

    }

    /*
     * PUT / PATCH HTTP methods
     */

    public TaskTemplate updateTask(
            UpdateTaskRequest request,
            long taskId
    ) throws NoSuchTaskException, NoSuchPriorityException {

        Priority priority = getPriorityById(
                request.getPriorityId()
        );

        Task task = getTaskById(
                taskId
        );

        task.update(
                request,
                priority
        );

        return TaskMapper
                .toTaskTemplate(
                        taskRepository.save(task)
                );
    }

    /**
     * Method for PATCH HTTP method. It allows to update only some fields of Task entity.
     * For example, if client wants to update only title and priority of task, he can send request with body: {"title": "New title", "priorityId": 2}
     * How it works:
     * 1. Find the task in database - if no task present - throw NoSuchTaskException
     * 2. Use beanWrapper to dynamically set task properties
     * 3. Handle special case of priority ID - if present in updates map, get priority from database, update task and remove priorityId from updates
     *    If no priority found under given priorityId - throw NoSuchPriorityException
     * 4. Update the rest of task using update map
     * @param updates Holds all fields of Task that needs to be updated - key is field name and value is new value of the field. For example: {"title": "New title", "priorityId": 2}
     *                Key name must be equal to Task field name
     * @param taskId ID of task
     * @throws NoSuchTaskException Thrown when no task were found under task ID
     * @throws NoSuchPriorityException Thrown when no Priority were found under priorityId key in updates map
     * @throws BeansException Thrown when key in updates map is not equal to any field name in Task entity or when value type is not compatible with field type
     */

    public TaskTemplate patchTask(
            Map<String, Object> updates,
            long taskId
    )  throws NoSuchTaskException, NoSuchPriorityException, BeansException {

        Task task = getTaskById(
                taskId
        );

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(task);

        if (updates.containsKey("priorityId")) {

            Long priorityId = (Long) updates.get("priorityId");

            Priority priority = getPriorityById(
                    priorityId
            );

            beanWrapper.setPropertyValue("priority", priority);

            updates.remove("priorityId");

        }

        beanWrapper.setPropertyValues(updates);

        return TaskMapper.toTaskTemplate(
                taskRepository.save(task)
        );
    }

    /*
     * DELETE HTTP method
     *
     * To delete task, task cannot have volunteer assigned to it
     */

    public void deleteTask(
            long taskId
    ) throws NoSuchTaskException {

        Task task = getTaskByIdWithVolunteers(
                taskId
        );

        if (!task.getVolunteerTasks().isEmpty()) {
            
            String volunteersInfo = TaskServiceLogger.formatVolunteerTasksList(task.getVolunteerTasks());
            TaskServiceLogger.taskIndelible(volunteersInfo);
            throw new AssignedVolunteersException(volunteersInfo);
            
        }

        taskRepository.deleteById(taskId);

    }

    /*
     * Task service logger
     */

    private static class TaskServiceLogger {

        public static void taskNotFound(long id) {
            log.error("Task with id {} not found", id);
        }

        public static void priorityNotFound(long id) {
            log.error("Priority with id {} not found", id);
        }
        
        public static void taskIndelible(String volunteersWithTasks) {
            log.error("Cannot delete task - volunteers are assigned to this task: {}", volunteersWithTasks);
        }

        /**
         * Formats a list of VolunteerTasks into a readable string representation
         * @param volunteersWithTasks List of VolunteerTask objects to format
         * @return Formatted string with volunteer and task IDs
         */
        public static String formatVolunteerTasksList(List<VolunteerTask> volunteersWithTasks) {
            StringBuilder sb = new StringBuilder();
            for (VolunteerTask volunteerTask : volunteersWithTasks) {
                sb.append("Volunteer Id: ")
                        .append(volunteerTask.getId().getVolunteerUserId())
                        .append(", TaskId: ")
                        .append(volunteerTask.getId().getTaskId())
                        .append("\n");
            }
            return sb.toString();
        }

    }

    /*
     * Helper methods
     */

    private Task getTaskById(
            long taskId
    ) throws NoSuchTaskException {

        return taskRepository
                .getTaskById(taskId)
                .orElseThrow(
                        () -> {
                            TaskServiceLogger.taskNotFound(taskId);
                            return new NoSuchTaskException(taskId);
                        }
                );

    }

    private Task getTaskByIdWithVolunteers(
            long taskId
    ) throws NoSuchTaskException {
        return taskRepository
                .getTaskByIdWithVolunteers(taskId)
                .orElseThrow(
                        () -> {
                            TaskServiceLogger.taskNotFound(taskId);
                            return new NoSuchTaskException(taskId);
                        }
                );
    }

    private Priority getPriorityById(
            long priorityId
    ) throws NoSuchPriorityException {

        return priorityService
                .getPriorityById(priorityId)
                .orElseThrow(
                        () -> {
                            TaskServiceLogger.priorityNotFound(priorityId);
                            return new NoSuchPriorityException(priorityId);
                        }
                );

    }

}
