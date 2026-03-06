package com.vpm.authenticationserver.exception.user;

import com.vpm.authenticationserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(
                "Invalid email or password",
                HttpStatus.BAD_REQUEST,
                "INVALID_CREDENTIALS"
        );
    }

}
