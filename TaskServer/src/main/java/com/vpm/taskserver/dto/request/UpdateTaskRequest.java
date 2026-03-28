package com.vpm.taskserver.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {

    private String title;

    private String description;

    private long priorityId;

    private LocalDate deadline;
}
