package com.vpm.taskserver.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class VolunteerAssignedToReportedTaskResponse {

    private Long taskId;
    private Long volunteerId;
    private LocalDate assignedAt;

}
