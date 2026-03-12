package com.vpm.organizationserver.exception.organization;

import com.vpm.common.error.ErrorCode;
import com.vpm.organizationserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class OrganizationAlreadyExistsException extends ApiException {
    public OrganizationAlreadyExistsException(String message, ErrorCode errorCode) {
        super(
                String.format("Cannot register organization: %s", message),
                HttpStatus.BAD_REQUEST,
                errorCode
        );
    }
}
