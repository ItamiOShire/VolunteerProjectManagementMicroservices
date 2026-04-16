package com.vpm.volunteerserver.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class VolunteerProfileResponse {

    // Combination of first and last name
    private String fullName;

    private LocalDate dateOfBirth;

    // Combination of volunteer contact email and phone number
    private String contact;

}
