package com.vpm.projectserver.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "title field cannot be blank")
    @Size(min = 5, max = 80, message = "title must be between 5 and 80 characters long")
    private String title;

    @NotBlank(message = "description field cannot be blank")
    private String description;

    @NotBlank(message = "imgPath field cannot be blank")
    @Size(max = 400, message = "imgPath cannot be longer than 400 characters")
    private String imgPath;

    @Positive(message = "organizationUserId must be a positive number")
    private long organizationUserId;

    @NotBlank(message = "organizationName field cannot be blank")
    @Size(max = 50, message = "organizationName cannot be longer than 50 characters")
    private String organizationName;

    @Valid
    private Set<@Positive Long> tagIds;

}
