package com.vpm.projectserver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProjectTemplate {

    /*
     * itemId field matches object's ID in database
     *
     * This is to help frontend use 'key' for mapping data to components
     */

    private long itemId;

    @NotBlank(message = "title field cannot be blank")
    @Size(min = 5, max = 80, message = "title must be between 5 and 80 characters long")
    private String projectTitle;

    @NotBlank(message = "organizationName field cannot be blank")
    @Size(max = 50, message = "organizationName cannot be longer than 50 characters")
    private String organizationName;

    @NotBlank(message = "description field cannot be blank")
    private String projectDescription;

    @NotBlank(message = "imgPath field cannot be blank")
    @Size(max = 400, message = "imgPath cannot be longer than 400 characters")
    private String imgPath;

    @Valid
    private List<TagTemplate> tags;

}
