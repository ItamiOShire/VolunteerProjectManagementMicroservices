package com.vpm.authenticationserver.exception.request;

import com.vpm.authenticationserver.exception.ApiException;
import com.vpm.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;

public class MissingRequiredHeaderException extends ApiException {
    public MissingRequiredHeaderException(List<String> headers) {
        super(
                String.format("Missing required headers: %s", headers.toString()),
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST
        );
    }
}
