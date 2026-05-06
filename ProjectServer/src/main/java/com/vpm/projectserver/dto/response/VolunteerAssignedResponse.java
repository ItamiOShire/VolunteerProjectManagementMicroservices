package com.vpm.projectserver.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VolunteerAssignedResponse {

    private Long projectId;
    private Long volunteerId;

}
