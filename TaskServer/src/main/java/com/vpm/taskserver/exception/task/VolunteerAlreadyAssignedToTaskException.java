package com.vpm.taskserver.exception.task;

import com.vpm.common.error.ErrorCode;
import com.vpm.taskserver.exception.ApiException;
import org.springframework.http.HttpStatus;

public class VolunteerAlreadyAssignedToTaskException extends ApiException {
    public VolunteerAlreadyAssignedToTaskException(Long volunteerId, Long taskId) {
        super(
                String.format("Volunteer with ID %d is already assigned to task with ID %d", volunteerId, taskId),
                HttpStatus.BAD_REQUEST,
                ErrorCode.ALREADY_ASSIGNED
        );
    }
}
