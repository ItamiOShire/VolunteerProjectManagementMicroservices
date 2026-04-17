package com.vpm.volunteerserver.service;


import com.vpm.volunteerserver.dto.response.VolunteerProfileResponse;
import com.vpm.volunteerserver.dto.template.VolunteerToAssignToTaskTemplate;
import com.vpm.volunteerserver.entity.Volunteer;
import com.vpm.volunteerserver.entity.mapper.VolunteerMapper;
import com.vpm.volunteerserver.exception.volunteer.NoSuchVolunteerException;
import com.vpm.volunteerserver.repository.VolunteerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;

    public VolunteerService(
            VolunteerRepository volunteerRepository
    ) {
        this.volunteerRepository = volunteerRepository;
    }

    /*
     * GET HTTP method
     */

    public VolunteerProfileResponse getVolunteerProfile(
            Long volunteerUserId
    ) throws NoSuchVolunteerException {

        Volunteer volunteer = getVolunteerByUserId(
                volunteerUserId
        );

        return VolunteerMapper.toVolunteerProfileResponse(
                volunteer
        );
    }

    public List<VolunteerProfileResponse> getVolunteersInProject(
            Long projectId
    ) {
        List<VolunteerProfileResponse> response = volunteerRepository
                .getVolunteersInProjectByProjectId(projectId)
                .stream()
                .map(VolunteerMapper::toVolunteerProfileResponse)
                .toList();

        if (response.isEmpty()) {
            log.warn("No volunteers in project");
        }

        return response;
    }

    public List<VolunteerProfileResponse> getVolunteersInProjectWhoAreAssignedToTask(
            Long projectId,
            Long taskId
    ) {
        return volunteerRepository
                .getVolunteersInProjectAssignedToTask(
                        projectId,
                        taskId
                )
                .stream()
                .map(VolunteerMapper::toVolunteerProfileResponse)
                .toList();
    }

    public List<VolunteerProfileResponse> getVolunteersInProjectWhoAreNotInTask(
            Long projectId,
            Long taskId
    ) {

        return getVolunteersNotAssignedToTask(
                projectId,
                taskId
        )
                .stream()
                .map(VolunteerMapper::toVolunteerProfileResponse)
                .toList();

    }

    public List<VolunteerToAssignToTaskTemplate> getVolunteersInProjectWhoReportedTaskSuggestion(
            Long projectId,
            Long taskId
    ) {
        return volunteerRepository
                .getVolunteersInProjectWhoReportedTaskSuggestion(
                        projectId,
                        taskId
                )
                .stream()
                .map(VolunteerMapper::toVolunteerToAssignToTaskTemplate)
                .toList();
    }

    /*
     * This method should return list of volunteers sorted by task suggestion (at the top, there are volunteers that reported task suggestion)
     * the rest of list are volunteers that did not reported task suggestion
     * How it works:
     * 1. List = Get volunteers that are assigned or not to task and filter by volunteerTasks emptiness, which indicates that volunteer is not assigned to task
     * 2. Set = Get volunteers that reported task suggestion and map them into set of volunteers ID
     * 3. Map volunteer to corresponding response (mapper does not affect ** reportedTaskSuggestion field! **)
     * 3. Filter List by checking if volunteer appears in Set, if yes - set reportedTaskSuggestionField to true, else - to false
     */

    public List<VolunteerToAssignToTaskTemplate> getVolunteersNotAssignedToTaskWithTaskSuggestion(
            Long projectId,
            Long taskId
    ) {

        List<Volunteer> volunteersAssignedOrNotToTask = getVolunteersNotAssignedToTask(
                projectId,
                taskId
        );

        Set<Long> volunteersIdWhoReportedTaskSuggestion = volunteerRepository
                .getVolunteersInProjectWhoReportedTaskSuggestion(
                        projectId,
                        taskId
                )
                .stream()
                .map(Volunteer::getUserId)
                .collect(Collectors.toSet());

        return volunteersAssignedOrNotToTask
                        .stream()
                        .map( volunteer -> {
                            boolean reportedTaskSuggestion = volunteersIdWhoReportedTaskSuggestion.contains(volunteer.getUserId());

                            VolunteerToAssignToTaskTemplate template = VolunteerMapper.toVolunteerToAssignToTaskTemplate(volunteer);
                            template.setReportedTaskSuggestion(reportedTaskSuggestion);

                            return template;
                        })
                        .toList();
    }

    /*
     * PUT / PATCH HTTP method
     */

    // TODO: implement PUT method with changing email and password (with auth service)

    public void patchVolunteerProfile(
            Map<String, Object> updates,
            Long volunteerUserId
    )  throws NoSuchVolunteerException, BeansException {

        log.info("Patching Volunteer Profile with user id {}", volunteerUserId);

        Volunteer volunteer = getVolunteerByUserId(
                volunteerUserId
        );


        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(volunteer);

        log.info("Patching: {}", updates);

        beanWrapper.setPropertyValues(updates);

        volunteerRepository.save(volunteer);

    }

    /*
     * Logger helper class
     */

    private static class Logger {

        public static void VolunteerNotFound(long volunteerUserId) {
            log.error("Volunteer with user id {} not found", volunteerUserId);
        }

    }

    /*
     * Helper methods
     */

    private Volunteer getVolunteerByUserId(
            long volunteerUserId
    ) throws NoSuchVolunteerException {

        return volunteerRepository.findByUserId(volunteerUserId)
                .orElseThrow(() -> {
                    Logger.VolunteerNotFound(volunteerUserId);
                    return new NoSuchVolunteerException(volunteerUserId);}
                );

    }

    private List<Volunteer> getVolunteersNotAssignedToTask(
            Long projectId,
            Long taskId
    ) {

        return volunteerRepository
                .getAllVolunteersInProjectAssignedOrNotToTask(
                        projectId,
                        taskId
                )
                .stream()
                .filter(volunteer -> volunteer.getVolunteerTasks().isEmpty())
                .toList();

    }

}
