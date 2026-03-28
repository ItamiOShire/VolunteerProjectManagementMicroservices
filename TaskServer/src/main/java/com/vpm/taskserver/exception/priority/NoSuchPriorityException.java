package com.vpm.taskserver.exception.priority;

import com.vpm.common.error.ErrorCode;
import com.vpm.taskserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NoSuchPriorityException extends ApiException {
    public NoSuchPriorityException(long priorityId) {
        super(
                String.format("Priority with id %d does not exists", priorityId),
                HttpStatus.BAD_REQUEST,
                ErrorCode.PRIORITY_NOT_FOUND
        );
    }
}
