package com.vpm.projectserver.exception.project;

import com.vpm.common.error.ErrorCode;
import com.vpm.projectserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class VolunteerAlreadyAssignedToProjectException extends ApiException {
    public VolunteerAlreadyAssignedToProjectException(Long volunteerId, Long projectId) {

        super(
                String.format("Volunteer with ID: %s is already assigned to project with ID: %s", volunteerId, projectId),
                HttpStatus.BAD_REQUEST,
                ErrorCode.ALREADY_ASSIGNED
        );
    }
}
