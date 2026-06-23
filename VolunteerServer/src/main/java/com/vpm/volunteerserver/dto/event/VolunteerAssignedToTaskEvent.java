package com.vpm.volunteerserver.dto.event;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class VolunteerAssignedToTaskEvent implements Serializable {

    private Long volunteerId;
    private Long taskId;
    private Long projectId;
    private LocalDate assignedDate;

}
