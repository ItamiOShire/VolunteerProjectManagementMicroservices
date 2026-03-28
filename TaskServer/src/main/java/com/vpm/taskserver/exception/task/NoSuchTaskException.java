package com.vpm.taskserver.exception.task;

import com.vpm.common.error.ErrorCode;
import com.vpm.taskserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NoSuchTaskException extends ApiException {
    public NoSuchTaskException(long id) {
        super(
                String.format("Task with id %d does not exists", id),
                HttpStatus.BAD_REQUEST,
                ErrorCode.TASK_NOT_FOUND
        );
    }
}
