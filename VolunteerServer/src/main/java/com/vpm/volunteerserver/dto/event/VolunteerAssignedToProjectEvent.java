package com.vpm.volunteerserver.dto.event;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class VolunteerAssignedToProjectEvent implements Serializable {

    private Long volunteerId;
    private Long projectId;
    private LocalDate assignedDate;

}
