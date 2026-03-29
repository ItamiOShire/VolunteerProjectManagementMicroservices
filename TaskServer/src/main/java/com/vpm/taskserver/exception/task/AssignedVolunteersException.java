package com.vpm.taskserver.exception.task;

import com.vpm.common.error.ErrorCode;
import com.vpm.taskserver.exception.ApiException;
import org.springframework.http.HttpStatus;


public class AssignedVolunteersException extends ApiException {

    public AssignedVolunteersException(String volunteersWithTasks) {
        super(
                String.format("Cannot delete task - volunteers are assigned to this task: %s", volunteersWithTasks),
                HttpStatus.BAD_REQUEST,
                ErrorCode.TASK_INDELIBLE
        );

    }
}
