package com.vpm.volunteerserver.service;


import com.vpm.volunteerserver.dto.response.VolunteerProfileResponse;
import com.vpm.volunteerserver.entity.Volunteer;
import com.vpm.volunteerserver.entity.mapper.VolunteerMapper;
import com.vpm.volunteerserver.exception.volunteer.NoSuchVolunteerException;
import com.vpm.volunteerserver.repository.VolunteerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

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

    private Volunteer getVolunteerByUserId(
            long volunteerUserId
    ) throws NoSuchVolunteerException {

        return volunteerRepository.findByUserId(volunteerUserId)
                .orElseThrow(() -> {
                    Logger.VolunteerNotFound(volunteerUserId);
                    return new NoSuchVolunteerException(volunteerUserId);}
                );

    }

}
