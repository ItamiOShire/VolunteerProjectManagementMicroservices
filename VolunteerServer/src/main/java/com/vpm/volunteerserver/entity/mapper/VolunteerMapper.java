package com.vpm.volunteerserver.entity.mapper;

import com.vpm.volunteerserver.dto.request.VolunteerRegisterRequest;
import com.vpm.volunteerserver.entity.Volunteer;

public class VolunteerMapper {

    public static Volunteer map(VolunteerRegisterRequest volunteer, long userId) {

        return Volunteer.builder()
                .userId(userId)
                .firstName(volunteer.getFirstName())
                .lastName(volunteer.getLastName())
                .dateOfBirth(volunteer.getDateOfBirth())
                .phoneNumber(volunteer.getPhoneNumber())
                .build();
    }

}
