package com.vpm.volunteerserver.service.event;

import com.vpm.volunteerserver.dto.event.VolunteerAssignedToProjectEvent;
import com.vpm.volunteerserver.dto.event.VolunteerAssignedToTaskEvent;
import com.vpm.volunteerserver.dto.event.VolunteerReportedTaskSuggestionEvent;
import com.vpm.volunteerserver.entity.TaskSuggestion;
import com.vpm.volunteerserver.entity.Volunteer;
import com.vpm.volunteerserver.entity.VolunteerProject;
import com.vpm.volunteerserver.entity.VolunteerTask;
import com.vpm.volunteerserver.entity.pks.TaskSuggestionId;
import com.vpm.volunteerserver.entity.pks.VolunteerProjectId;
import com.vpm.volunteerserver.entity.pks.VolunteerTaskId;
import com.vpm.volunteerserver.exception.volunteer.NoSuchVolunteerException;
import com.vpm.volunteerserver.repository.VolunteerRepository;
import com.vpm.volunteerserver.service.util.ServiceUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class VolunteerEventService {

    private final VolunteerRepository volunteerRepository;
    private final ServiceUtils serviceUtils;

    /*
     * Event handler method
     */

    public VolunteerProject assignVolunteerToProject(
            VolunteerAssignedToProjectEvent event
    ) throws NoSuchVolunteerException {

        Volunteer volunteer = serviceUtils.getVolunteerByUserIdOrThrow(
                event.getVolunteerId()
        );

        VolunteerProject volunteerProject = VolunteerProject.builder()
                .volunteerProjectId( new VolunteerProjectId(
                        volunteer.getUserId(),
                        event.getProjectId()
                ))
                .volunteer(volunteer)
                .projectId(event.getProjectId())
                .build();

        volunteer.getVolunteerProjects().add(volunteerProject);

        volunteerRepository.save(volunteer);

        return volunteerProject;
    }

    public VolunteerTask assignVolunteerToTask(
            VolunteerAssignedToTaskEvent event
    ) throws NoSuchVolunteerException {

        Volunteer volunteer = serviceUtils.getVolunteerByUserIdOrThrow(
                event.getVolunteerId()
        );

        VolunteerTask volunteerTask = VolunteerTask.builder()
                .volunteerTaskId(new VolunteerTaskId(
                        volunteer.getUserId(),
                        event.getTaskId(),
                        event.getProjectId()
                ))
                .volunteer(volunteer)
                .taskId(event.getTaskId())
                .build();

        volunteer.getVolunteerTasks().add(volunteerTask);

        volunteerRepository.save(volunteer);

        return volunteerTask;
    }

    public TaskSuggestion assignVolunteerToSuggestedTask(
            VolunteerReportedTaskSuggestionEvent event
    ) throws NoSuchVolunteerException {

        Volunteer volunteer = serviceUtils.getVolunteerByUserIdOrThrow(event.getVolunteerId());

        TaskSuggestion taskSuggestion = TaskSuggestion.builder()
                .taskSuggestionId(
                        new TaskSuggestionId(
                                volunteer.getUserId(),
                                event.getTaskId(),
                                event.getProjectId()
                        )
                )
                .volunteer(volunteer)
                .taskId(event.getTaskId())
                .build();

        volunteer.getVolunteerTaskSuggestions().add(taskSuggestion);

        volunteerRepository.save(volunteer);

        return taskSuggestion;
    }

}
