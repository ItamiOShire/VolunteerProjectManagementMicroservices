package com.vpm.projectserver.dto.response;

import lombok.Builder;

@Builder
public class VolunteerAssignedResponse {

    private long projectId;
    private long volunteerId;

}
