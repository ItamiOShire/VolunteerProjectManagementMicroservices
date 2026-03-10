package com.vpm.common.error;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private LocalDateTime timestamp;
    private ErrorCode errorCode;
    private int status;
    private String message;
    private String path;

    public ErrorResponse(
            int status,
            ErrorCode errorCode,
            String message,
            String path
    ) {
        this.timestamp = LocalDateTime.now();
        this.errorCode = errorCode;
        this.status = status;
        this.message = message;
        this.path = path;
    }

}
