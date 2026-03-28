package com.vpm.taskserver.service;

import com.vpm.taskserver.dto.request.CreateTaskRequest;
import com.vpm.taskserver.dto.request.UpdateTaskRequest;
import com.vpm.taskserver.dto.template.TaskTemplate;
import com.vpm.taskserver.entity.Priority;
import com.vpm.taskserver.entity.Task;
import com.vpm.taskserver.entity.mapper.TaskMapper;
import com.vpm.taskserver.exception.priority.NoSuchPriorityException;
import com.vpm.taskserver.exception.task.NoSuchTaskException;
import com.vpm.taskserver.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
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

        return taskRepository.findAll().stream()
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

    public void createTask(
            CreateTaskRequest request
    ) throws NoSuchPriorityException {

        Optional<Priority> priority = priorityService.getPriorityById(
                request.getPriorityId()
        );

        if (priority.isEmpty()) {
            log.error("Priority not found for id {}", request.getPriorityId());
            throw new NoSuchPriorityException(request.getPriorityId());
        }

        Task task = TaskMapper.fromCreateTaskRequest(
                request,
                priority.get()
        );

        taskRepository.save(task);

    }

    /*
     * PUT / PATCH HTTP methods
     */

    public void updateTask(
            UpdateTaskRequest request,
            long taskId
    ) throws NoSuchTaskException, NoSuchPriorityException {

        Optional<Priority> priority = priorityService.getPriorityById(
                request.getPriorityId()
        );

        if (priority.isEmpty()) {
            log.error("Priority not found for id {}", request.getPriorityId());
            throw new NoSuchPriorityException(request.getPriorityId());
        }

        Optional<Task> task = taskRepository.findById(taskId);

        if (task.isEmpty()) {
            log.error("Task not found for id {}", taskId);
            throw new NoSuchTaskException(taskId);
        }

        Task taskToUpdate = task.get();
        taskToUpdate.update(
                request,
                priority.get()
        );

        taskRepository.save(taskToUpdate);
    }

    public void patchTask(
            Map<String, Object> updates,
            long taskId
    )  throws NoSuchTaskException, NoSuchPriorityException {



    }

}
