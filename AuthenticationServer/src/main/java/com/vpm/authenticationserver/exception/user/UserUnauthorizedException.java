package com.vpm.authenticationserver.exception.user;

import com.vpm.authenticationserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserUnauthorizedException extends ApiException {

    public UserUnauthorizedException()  {
        super (
                "User unauthorized",
                HttpStatus.UNAUTHORIZED,
                "USER_UNAUTHORIZED"
        );
    }

}
