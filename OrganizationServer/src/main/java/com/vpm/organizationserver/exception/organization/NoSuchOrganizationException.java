package com.vpm.organizationserver.exception.organization;

import com.vpm.common.error.ErrorCode;
import com.vpm.organizationserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NoSuchOrganizationException extends ApiException {
    public NoSuchOrganizationException(long id) {
        super(
                String.format("Organization with id %d does not exist", id),
                HttpStatus.NOT_FOUND,
                ErrorCode.USER_NOT_FOUND
        );
    }
}
