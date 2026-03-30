package com.vpm.organizationserver.exception;

import com.vpm.common.error.ErrorCode;
import com.vpm.common.error.ErrorResponse;
import com.vpm.organizationserver.exception.request.MissingRequiredHeaderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
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

    @ExceptionHandler(MissingRequiredHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(
            MissingRequestHeaderException e,
            WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                400,
                ErrorCode.BAD_REQUEST,
                "Error during request processing: missing header " + e.getHeaderName(),
                request.getContextPath()
        );

        log.error("Error during request processing: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
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
