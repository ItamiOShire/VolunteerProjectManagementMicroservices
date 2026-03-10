package com.vpm.volunteerserver.exception;

import com.vpm.common.error.ErrorCode;
import com.vpm.common.error.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException e,
            WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                e.getStatus().value(),
                e.getErrorCode(),
                e.getMessage(),
                request.getContextPath()
        );

        return ResponseEntity
                .status(e.getStatus())
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                500,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getContextPath()
        );

        return ResponseEntity
                .status(500)
                .body(error);
    }

}
