package com.vpm.volunteerserver.dto.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class VolunteerToAssignToTaskTemplate {

    private long itemId;

    // Combination of first and last name
    private String fullName;

    private String contact;

    // Combination of volunteer contact email and phone number
    @Setter
    private boolean reportedTaskSuggestion;

}
