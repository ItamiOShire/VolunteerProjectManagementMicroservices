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
import java.util.Optional;

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
     * Logger helper class
     */

    private static class Logger {

        public static void VolunteerNotFound(long volunteerUserId) {
            log.error("Volunteer with user id {} not found", volunteerUserId);
        }

    }


    /*
     * GET HTTP method
     */

    public VolunteerProfileResponse getVolunteerProfile(
            Long volunteerUserId
    ) throws NoSuchVolunteerException {

        Volunteer volunteer =
                volunteerRepository.findByUserId(volunteerUserId)
                        .orElseThrow(() -> {
                            Logger.VolunteerNotFound(volunteerUserId);
                            return new NoSuchVolunteerException(volunteerUserId);}
                        );

        return VolunteerMapper.toVolunteerProfileResponse(
                volunteer
        );
    }

    /*
     * PUT / PATCH HTTP method
     */

    public void patchVolunteerProfile(
            Map<String, Object> updates,
            Long volunteerUserId
    )  throws NoSuchVolunteerException, BeansException {

        log.info("Patching Volunteer Profile with user id {}", volunteerUserId);

        Volunteer volunteer =
                volunteerRepository.findByUserId(volunteerUserId)
                        .orElseThrow(() -> {
                            Logger.VolunteerNotFound(volunteerUserId);
                            return new NoSuchVolunteerException(volunteerUserId);}
                        );

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(volunteer);

        log.info("Patching...");

        beanWrapper.setPropertyValues(updates);

        volunteerRepository.save(volunteer);

    }

}
