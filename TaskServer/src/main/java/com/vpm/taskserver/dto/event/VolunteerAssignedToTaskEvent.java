package com.vpm.taskserver.dto.event;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
public class VolunteerAssignedToTaskEvent implements Serializable {

    private Long volunteerId;
    private Long taskId;
    private Long projectId;
    private LocalDate createdAt;

}
