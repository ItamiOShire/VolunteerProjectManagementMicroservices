package com.vpm.authenticationserver.exception.request;

import com.vpm.authenticationserver.exception.ApiException;
import com.vpm.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidServiceNameHeaderException extends ApiException {
    public InvalidServiceNameHeaderException(String headerValue) {
        super(
                String.format("Invalid server access - request form service: %s", headerValue),
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST
        );
    }
}
