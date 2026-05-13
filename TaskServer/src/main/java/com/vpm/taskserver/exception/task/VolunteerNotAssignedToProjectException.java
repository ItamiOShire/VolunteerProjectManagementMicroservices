package com.vpm.taskserver.exception.task;

import com.vpm.common.error.ErrorCode;
import com.vpm.taskserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class VolunteerNotAssignedToProjectException extends ApiException {
    public VolunteerNotAssignedToProjectException(String message, ErrorCode errorCode) {
        super(
                String.format("Cannot assign volunteer to task - %s", message),
                HttpStatus.BAD_REQUEST,
                errorCode
        );
    }
}
