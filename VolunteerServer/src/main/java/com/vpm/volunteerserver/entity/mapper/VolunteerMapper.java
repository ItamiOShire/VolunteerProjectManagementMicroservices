package com.vpm.volunteerserver.entity.mapper;

import com.vpm.common.dto.response.AuthRegistrationResponse;
import com.vpm.volunteerserver.dto.request.VolunteerRegisterRequest;
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

}
