package com.vpm.volunteerserver.dto.event;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VolunteerAssignedToProjectEvent {

    private Long volunteerId;
    private Long projectId;
    private LocalDate assignedDate;

}
