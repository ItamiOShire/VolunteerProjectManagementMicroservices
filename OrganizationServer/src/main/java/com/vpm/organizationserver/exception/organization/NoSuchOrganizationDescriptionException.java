package com.vpm.organizationserver.exception.organization;

import com.vpm.common.error.ErrorCode;
import com.vpm.organizationserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NoSuchOrganizationDescriptionException extends ApiException {
    public NoSuchOrganizationDescriptionException(long id) {
        super(
                String.format("No organization description found for organization with id %d", id),
                HttpStatus.NOT_FOUND,
                ErrorCode.PROFILE_NOT_FOUND
        );
    }
}
