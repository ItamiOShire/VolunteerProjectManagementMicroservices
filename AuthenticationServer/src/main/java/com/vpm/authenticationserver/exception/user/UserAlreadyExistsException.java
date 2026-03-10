package com.vpm.authenticationserver.exception.user;

import com.vpm.authenticationserver.exception.ApiException;
import com.vpm.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends ApiException {
    public UserAlreadyExistsException(String email) {
        super(
                String.format("User with email '%s' already exists", email),
                HttpStatus.valueOf(409),
                ErrorCode.USER_ALREADY_EXISTS
        );
    }
}
