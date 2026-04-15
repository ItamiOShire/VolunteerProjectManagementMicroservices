package com.vpm.projectserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagTemplate {

    /*
     * itemId field matches object's ID in database
     *
     * This is to help frontend use 'key' for mapping data to components
     */

    @Positive(message = "itemId must be a positive number")
    private long itemId;

    @NotBlank(message = "tag name field cannot be blank")
    @Size(max = 40, message = "tag name cannot be longer than 40 characters")
    private String tagName;

}
