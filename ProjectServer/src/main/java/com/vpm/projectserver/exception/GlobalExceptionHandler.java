package com.vpm.projectserver.exception;


import com.vpm.common.error.ErrorCode;
import com.vpm.common.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /*
     * Developer defined exceptions
     */

    /**
     * Exceptions thrown and logged in business layer
     * @param ex custom exception
     * @param request client request
     * @return ResponseEntity with http status and body of ErrorResponse
     */

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException ex,
            WebRequest request
    ) {
       ErrorResponse error = new ErrorResponse(
               ex.getStatus().value(),
               ex.getErrorCode(),
               ex.getMessage(),
               request.getContextPath()
       );

       return ResponseEntity
               .status(ex.getStatus())
               .body(error);

    }

    /*
     * Spring thrown exceptions
     */

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageConversionException(
            HttpMessageConversionException e,
            WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                400,
                ErrorCode.BAD_REQUEST,
                "Error during body conversion: Exception - " + e.getClass().getName() + ", message - " + e.getMessage(),
                request.getContextPath()
        );

        log.error("Error during body conversion: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
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

    /*
     * Undefined exceptions
     */

    /**
     * Unexpected exceptions occurring in any layer. Should be logged there
     * @param e exception
     * @param request client request for getting path
     * @return
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            WebRequest request
    ) {
        ErrorResponse error = new ErrorResponse(
                500,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + e.getClass().getName() + " - " + e.getMessage() ,
                request.getContextPath()
        );

        log.error("An unexpected error occurred: {} - {}", e.getClass().getName(), e.getMessage());

        return ResponseEntity
                .status(500)
                .body(error);
    }

}
