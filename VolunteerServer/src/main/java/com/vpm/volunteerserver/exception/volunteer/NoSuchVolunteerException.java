package com.vpm.volunteerserver.exception.volunteer;

import com.vpm.common.error.ErrorCode;
import com.vpm.volunteerserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NoSuchVolunteerException extends ApiException {
    public NoSuchVolunteerException(long volunteerUserId) {
        super(
                String.format("No volunteer found with user ID: %d", volunteerUserId),
                HttpStatus.NOT_FOUND,
                ErrorCode.USER_NOT_FOUND
        );
    }
}
