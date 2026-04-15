package com.vpm.taskserver.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTaskRequest {

    @Positive
    private long projectId;

    @Positive
    private long priorityId;

    @NotBlank(message = "Title must not be blank")
    @Size(min = 5, max = 50, message = "Title must be between 5 and 50 characters long")
    private String title;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @NotBlank(message = "Deadline must not be blank")
    @Future(message = "Deadline must be a future date")
    private LocalDate deadline;

}
