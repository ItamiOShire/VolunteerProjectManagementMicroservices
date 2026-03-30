package com.vpm.volunteerserver.entity.mapper;

import com.vpm.common.dto.response.AuthRegistrationResponse;
import com.vpm.volunteerserver.dto.request.VolunteerRegisterRequest;
import com.vpm.volunteerserver.dto.response.VolunteerProfileResponse;
import com.vpm.volunteerserver.entity.Volunteer;

public class VolunteerMapper {

    public static Volunteer mapFromRegisterRequestAndAuthResponse(
            VolunteerRegisterRequest request, AuthRegistrationResponse response) {

        return Volunteer.builder()
                .userId(response.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }

    public static VolunteerProfileResponse toVolunteerProfileResponse(
            Volunteer volunteer
    ) {
        StringBuilder fullName = new StringBuilder();
        fullName.append(volunteer.getFirstName())
                .append(" ")
                .append(volunteer.getLastName());

        return VolunteerProfileResponse.builder()
                .fullName(fullName.toString())
                .dateOfBirth(volunteer.getDateOfBirth())
                .contact(volunteer.getPhoneNumber())
                .build();
    }

}
