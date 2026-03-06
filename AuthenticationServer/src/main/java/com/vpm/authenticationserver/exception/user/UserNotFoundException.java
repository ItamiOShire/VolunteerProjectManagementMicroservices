package com.vpm.authenticationserver.exception.user;

import com.vpm.authenticationserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(String email) {
        super(
                String.format("User with email '%s' not found", email),
                HttpStatus.NOT_FOUND,
                "USER_NOT_FOUND"
        );
    }

}
