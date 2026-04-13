package com.vpm.taskserver.dto.template;

import lombok.Builder;

import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TaskTemplate {

    private long itemId;

    private String title;

    private String description;

    private String priorityName;

    private LocalDate deadline;

}
