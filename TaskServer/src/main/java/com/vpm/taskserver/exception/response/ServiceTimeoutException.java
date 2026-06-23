package com.vpm.taskserver.exception.response;

import com.vpm.common.error.ErrorCode;
import com.vpm.taskserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ServiceTimeoutException extends ApiException {
    public ServiceTimeoutException(String message) {

        super(
                String.format("Service timeout calling: %s", message),
                HttpStatus.SERVICE_UNAVAILABLE,
                ErrorCode.SERVICE_UNAVAILABLE
        );
    }
}
