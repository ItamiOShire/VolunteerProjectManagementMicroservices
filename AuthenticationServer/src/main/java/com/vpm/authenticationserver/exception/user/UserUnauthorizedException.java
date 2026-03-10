package com.vpm.authenticationserver.exception.user;

import com.vpm.authenticationserver.exception.ApiException;
import com.vpm.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class UserUnauthorizedException extends ApiException {

    public UserUnauthorizedException()  {
        super (
                "User unauthorized",
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED
        );
    }

}
