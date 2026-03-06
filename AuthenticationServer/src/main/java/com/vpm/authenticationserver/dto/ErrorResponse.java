package com.vpm.authenticationserver.dto;


import java.time.LocalDateTime;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private String errorCode;
    private int status;
    private String message;
    private String path;

    public ErrorResponse(
            int status,
            String errorCode,
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
