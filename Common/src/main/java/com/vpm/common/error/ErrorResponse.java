package com.vpm.common.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
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
