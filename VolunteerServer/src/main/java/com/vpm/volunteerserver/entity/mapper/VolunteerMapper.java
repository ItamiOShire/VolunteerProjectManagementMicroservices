package com.vpm.volunteerserver.entity.mapper;

import com.vpm.common.dto.response.AuthRegistrationResponse;
import com.vpm.volunteerserver.dto.request.VolunteerRegisterRequest;
import com.vpm.volunteerserver.dto.response.VolunteerProfileResponse;
import com.vpm.volunteerserver.dto.template.VolunteerToAssignToTaskTemplate;
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
                .contactEmail(request.getEmail())
                .build();
    }

    public static VolunteerProfileResponse toVolunteerProfileResponse(
            Volunteer volunteer
    ) {
        return VolunteerProfileResponse.builder()
                .fullName(buildFullName(volunteer))
                .dateOfBirth(volunteer.getDateOfBirth())
                .contact(buildContactData(volunteer))
                .build();
    }

    public static VolunteerToAssignToTaskTemplate toVolunteerToAssignToTaskTemplate(
            Volunteer volunteer
    ) {

        return VolunteerToAssignToTaskTemplate.builder()
                .itemId(volunteer.getUserId())
                .fullName(buildFullName(volunteer))
                .build();

    }

    private static String buildFullName(
            Volunteer volunteer
    ) {
        StringBuilder fullName = new StringBuilder();

        fullName.append(volunteer.getFirstName())
                .append(" ")
                .append(volunteer.getLastName());

        return fullName.toString();
    }

    private static String buildContactData(
            Volunteer volunteer
    ) {

        StringBuilder contactData = new StringBuilder();

        contactData
                .append(volunteer.getContactEmail())
                .append(" / ")
                .append(volunteer.getPhoneNumber());

        return contactData.toString();
    }

}
