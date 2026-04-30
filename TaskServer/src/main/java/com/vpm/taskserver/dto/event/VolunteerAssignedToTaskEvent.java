package com.vpm.taskserver.dto.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class VolunteerAssignedToTaskEvent {

    private Long volunteerId;
    private Long taskId;
    private LocalDate createdAt;

}
