package com.vpm.volunteerserver.service.util;

import com.vpm.volunteerserver.entity.Volunteer;
import com.vpm.volunteerserver.exception.volunteer.NoSuchVolunteerException;
import com.vpm.volunteerserver.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceUtils {

    private final VolunteerRepository volunteerRepository;

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

    public Volunteer getVolunteerByUserIdOrThrow(
            long volunteerUserId
    ) throws NoSuchVolunteerException {

        return volunteerRepository.findByUserId(volunteerUserId)
                .orElseThrow(() -> {
                    Logger.VolunteerNotFound(volunteerUserId);
                    return new NoSuchVolunteerException(volunteerUserId);}
                );

    }

    public List<Volunteer> getVolunteersNotAssignedToTask(
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
