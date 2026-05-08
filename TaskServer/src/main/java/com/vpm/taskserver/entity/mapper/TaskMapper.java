package com.vpm.taskserver.entity.mapper;

import com.vpm.taskserver.dto.request.CreateTaskRequest;
import com.vpm.taskserver.dto.response.VolunteerAssignedToTaskResponse;
import com.vpm.taskserver.dto.template.TaskTemplate;
import com.vpm.taskserver.entity.Priority;
import com.vpm.taskserver.entity.Task;
import com.vpm.taskserver.entity.TaskSuggestion;
import com.vpm.taskserver.entity.VolunteerTask;

public class TaskMapper {

    public static TaskTemplate toTaskTemplate(Task task){

        return TaskTemplate.builder()
                .itemId(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priorityName(
                        task
                                .getPriority()
                                .getName()
                )
                .deadline(task.getDeadline())
                .build();
    }

    public static Task fromCreateTaskRequest(
            CreateTaskRequest request,
            Priority priority){

        Task task = Task.builder()
                .description(request.getDescription())
                .title(request.getTitle())
                .deadline(request.getDeadline())
                .projectId(request.getProjectId())
                .priority(priority)
                .build();

        priority.getTasks().add(task);

        return task;

    }

    public static VolunteerAssignedToTaskResponse fromVolunteerTaskToVolunteerAssignedToTaskResponse(
            VolunteerTask volunteerTask
    ) {
        return VolunteerAssignedToTaskResponse.builder()
                .taskId(volunteerTask.getId().getTaskId())
                .volunteerId(volunteerTask.getId().getVolunteerUserId())
                .build();
    }

    public static VolunteerAssignedToTaskResponse fromVolunteerTaskToVolunteerAssignedToReportedTaskSuggestion(
            TaskSuggestion taskSuggestion
    ) {
        return VolunteerAssignedToTaskResponse.builder()
                .taskId(taskSuggestion.getId().getTaskId())
                .volunteerId(taskSuggestion.getId().getVolunteerUserId())
                .build();
    }

}
