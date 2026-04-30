package com.vpm.taskserver.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VolunteerAssignedToTaskResponse {

    private Long taskId;
    private Long volunteerId;

}
