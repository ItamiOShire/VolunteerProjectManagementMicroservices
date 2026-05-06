package com.vpm.projectserver.dto.event;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
public class VolunteerAssignedToProjectEvent implements Serializable {

    private Long projectId;
    private Long volunteerId;
    private LocalDate createdAt;

}
