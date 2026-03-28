package com.vpm.taskserver.entity.mapper;

import com.vpm.taskserver.dto.template.PriorityTemplate;
import com.vpm.taskserver.entity.Priority;

public class PriorityMapper {

    public static PriorityTemplate toPriorityTemplate(Priority priority){
        return PriorityTemplate.builder()
                .itemId(priority.getId())
                .priorityName(priority.getName())
                .build();
    }

}
