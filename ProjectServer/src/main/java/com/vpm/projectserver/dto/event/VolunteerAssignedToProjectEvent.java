package com.vpm.projectserver.dto.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class VolunteerAssignedToProjectEvent {

    private long projectId;
    private long volunteerId;
    private LocalDate createdAt;

}
