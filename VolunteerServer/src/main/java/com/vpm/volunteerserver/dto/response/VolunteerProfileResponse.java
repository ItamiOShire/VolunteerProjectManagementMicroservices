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

    private String contact;

}
