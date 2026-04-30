package com.vpm.projectserver.entity.mapper;

import com.vpm.projectserver.dto.event.VolunteerAssignedToProjectEvent;
import com.vpm.projectserver.entity.ProjectVolunteer;

import java.time.LocalDate;

public class EventMapper {

    public static VolunteerAssignedToProjectEvent formProjectVolunteer(
            ProjectVolunteer projectVolunteer
    ) {

        return VolunteerAssignedToProjectEvent.builder()
                .volunteerId(projectVolunteer.getProjectVolunteerId().getVolunteerUserId())
                .projectId(projectVolunteer.getProjectVolunteerId().getProjectId())
                .createdAt(LocalDate.now())
                .build();

    }

}
