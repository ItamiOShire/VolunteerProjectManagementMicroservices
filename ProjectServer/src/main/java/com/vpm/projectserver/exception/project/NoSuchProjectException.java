package com.vpm.projectserver.exception.project;

import com.vpm.common.error.ErrorCode;
import com.vpm.projectserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NoSuchProjectException extends ApiException {
    public NoSuchProjectException(long  projectId) {
        super(
                String.format("Project with id {%d} does not exist.", projectId),
                HttpStatus.NOT_FOUND,
                ErrorCode.PROJECT_NOT_FOUND
        );
    }
}
