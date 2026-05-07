package com.vpm.authenticationserver.exception.user;

import com.vpm.authenticationserver.exception.ApiException;
import com.vpm.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiException {
    public InvalidRefreshTokenException(String token) {

        super(
                String.format("Invalid refresh token: %s", token),
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST
        );

    }
}
