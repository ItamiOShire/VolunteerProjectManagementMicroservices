package com.vpm.authenticationserver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import com.vpm.common.error.ErrorCode;

@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;

    public ApiException(
            String message,
            HttpStatus status,
            ErrorCode errorCode
    ) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

}
