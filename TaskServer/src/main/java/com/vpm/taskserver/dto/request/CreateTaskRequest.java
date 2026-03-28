package com.vpm.taskserver.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTaskRequest {

    private long projectId;

    private long priorityId;

    private String title;

    private String description;

    private LocalDate deadline;

}
