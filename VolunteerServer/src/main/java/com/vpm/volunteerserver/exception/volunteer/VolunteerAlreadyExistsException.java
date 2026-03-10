package com.vpm.volunteerserver.exception.volunteer;

import com.vpm.common.error.ErrorCode;
import com.vpm.volunteerserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class VolunteerAlreadyExistsException extends ApiException {
    public VolunteerAlreadyExistsException(String message, ErrorCode errorCode) {
        super(
                String.format("Cannot register volunteer: %s", message),
                HttpStatus.valueOf(409),
                errorCode
        );
    }
}
