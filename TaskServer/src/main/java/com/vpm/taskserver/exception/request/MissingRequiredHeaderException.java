package com.vpm.taskserver.exception.request;

import com.vpm.common.error.ErrorCode;
import com.vpm.taskserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class MissingRequiredHeaderException extends ApiException {
    public MissingRequiredHeaderException(String header) {
        super(
                String.format("Missing required header: %s", header),
                HttpStatus.BAD_REQUEST,
                ErrorCode.MISSING_HEADER
        );
    }
}
