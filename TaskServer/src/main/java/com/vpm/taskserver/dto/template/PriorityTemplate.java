package com.vpm.taskserver.dto.template;

import lombok.Builder;

@Builder
public class PriorityTemplate {

    private long itemId;
    private String priorityName;

}
