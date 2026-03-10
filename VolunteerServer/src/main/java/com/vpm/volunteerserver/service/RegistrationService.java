package com.vpm.volunteerserver.service;


import com.vpm.volunteerserver.api.internal.AuthClient;
import com.vpm.volunteerserver.dto.request.VolunteerRegisterRequest;
import com.vpm.common.dto.request.AuthRequest;
import com.vpm.common.dto.response.AuthResponse;
import com.vpm.volunteerserver.entity.Volunteer;
import com.vpm.volunteerserver.entity.mapper.VolunteerMapper;
import com.vpm.volunteerserver.exception.volunteer.VolunteerAlreadyExistsException;
import com.vpm.volunteerserver.repository.VolunteerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RegistrationService {

    private final VolunteerRepository volunteerRepository;
    private final AuthClient authClient;

    @Autowired
    public RegistrationService(VolunteerRepository volunteerRepository, AuthClient authClient) {
        this.volunteerRepository = volunteerRepository;
        this.authClient = authClient;
    }

    private void validateException(Exception e) {
        if (e instanceof VolunteerAlreadyExistsException) {
            log.error("Error during registration: {}", e.getMessage());
        } else {
            log.error("An unexpected error occurred: {}", e.getMessage());
        }
    }

    public void registerVolunteer(
            VolunteerRegisterRequest request
    ) {

        log.info("Registering volunteer with email: {}", request.getEmail());

        AuthRequest authRequest = new AuthRequest(
                request.getEmail(),
                request.getPassword(),
                "VOLUNTEER"
        );

        try {

            log.info("Sending registration request to auth service for email: {}", request.getEmail());

            AuthResponse authResponse = authClient.registerUserInAuthService(authRequest);

            log.info("Received response from auth service with userId: {}", authResponse.getUserId());

            Volunteer volunteer = VolunteerMapper.map(request, authResponse.getUserId());

            log.info("Saving volunteer: {}", volunteer.toString());

            Volunteer saved = volunteerRepository.save(volunteer);

            log.info("Saving successful - volunteer id: {}", saved.getId());

        } catch (Exception e) {

            validateException(e);

            throw e;

        }


    }

}
