package com.vpm.taskserver.entity.mapper;

import com.vpm.taskserver.dto.event.VolunteerAssignedToTaskEvent;
import com.vpm.taskserver.dto.event.VolunteerReportedTaskSuggestionEvent;
import com.vpm.taskserver.entity.TaskSuggestion;
import com.vpm.taskserver.entity.VolunteerTask;

import java.time.LocalDate;

public class EventMapper {

    public static VolunteerAssignedToTaskEvent fromTaskVolunteer(
            VolunteerTask volunteerTask
    ) {
        return VolunteerAssignedToTaskEvent.builder()
                .taskId(volunteerTask.getId().getTaskId())
                .volunteerId(volunteerTask.getId().getVolunteerUserId())
                .createdAt(LocalDate.now())
                .build();
    }

    public static VolunteerReportedTaskSuggestionEvent fromTaskSuggestionReport(
            TaskSuggestion taskSuggestion
    ) {
        return VolunteerReportedTaskSuggestionEvent.builder()
                .taskId(taskSuggestion.getId().getTaskId())
                .volunteerId(taskSuggestion.getId().getVolunteerUserId())
                .createdAt(LocalDate.now())
                .build();
    }

}
