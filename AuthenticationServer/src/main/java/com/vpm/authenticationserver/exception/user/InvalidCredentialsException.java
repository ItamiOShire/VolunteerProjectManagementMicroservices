package com.vpm.authenticationserver.exception.user;

import com.vpm.authenticationserver.exception.ApiException;
import com.vpm.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(
                "Invalid email or password",
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_CREDENTIALS
        );
    }

}
