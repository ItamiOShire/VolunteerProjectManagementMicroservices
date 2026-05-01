package com.vpm.volunteerserver.dto.event;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VolunteerAssignedToTaskEvent {

    private Long volunteerId;
    private Long taskId;
    private LocalDate assignedDate;

}
