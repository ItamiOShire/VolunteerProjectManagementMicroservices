package com.vpm.volunteerserver.exception.request;

import com.vpm.common.error.ErrorCode;
import com.vpm.volunteerserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class MissingRequiredHeaderException extends ApiException {
    public MissingRequiredHeaderException(String headerName) {
        super(
                String.format("Missing required header: %s", headerName),
                HttpStatus.BAD_REQUEST,
                ErrorCode.MISSING_HEADER
        );
    }
}

