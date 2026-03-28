package com.vpm.taskserver.dto.template;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public class TaskTemplate {

    private long itemId;

    private String title;

    private String description;

    private String priorityName;

    private LocalDate deadline;

}
