package com.vpm.taskserver.entity.mapper;

import com.vpm.taskserver.dto.request.CreateTaskRequest;
import com.vpm.taskserver.dto.template.TaskTemplate;
import com.vpm.taskserver.entity.Priority;
import com.vpm.taskserver.entity.Task;

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

        return Task.builder()
                .description(request.getDescription())
                .title(request.getTitle())
                .deadline(request.getDeadline())
                .projectId(request.getProjectId())
                .priority(priority)
                .build();

    }



}
