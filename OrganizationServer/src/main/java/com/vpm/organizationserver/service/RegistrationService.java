package com.vpm.organizationserver.service;


import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import com.vpm.organizationserver.api.internal.AuthClient;
import com.vpm.organizationserver.dto.request.OrganizationRegisterRequest;
import com.vpm.organizationserver.entity.Organization;
import com.vpm.organizationserver.entity.mapper.OrganizationMapper;
import com.vpm.organizationserver.exception.organization.OrganizationAlreadyExistsException;
import com.vpm.organizationserver.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RegistrationService {

    private final OrganizationRepository organizationRepository;
    private final AuthClient authClient;

    @Autowired
    public RegistrationService(
            OrganizationRepository repository, AuthClient authClient) {
        this.organizationRepository = repository;
        this.authClient = authClient;
    }

    private void validateException(Exception e) {
        if (e instanceof OrganizationAlreadyExistsException) {
            log.error("Error during registration: {}", e.getMessage());
        } else {
            log.error("An unexpected error occurred: {}", e.getMessage());
        }
    }

    public void register(
            OrganizationRegisterRequest request
    ) throws OrganizationAlreadyExistsException {

        AuthRegistrationRequest authRequest = new AuthRegistrationRequest(
                request.getEmail(),
                request.getPassword(),
                "ORGANIZATION"
        );

        try {

            log.info("Sending registration request to auth service for email: {}", request.getEmail());

            AuthRegistrationResponse authRegistrationResponse = authClient.registerOrganizationInAuthServer(authRequest);

            log.info("Received response from auth service with userId: {}", authRegistrationResponse.getUserId());

            Organization organization = OrganizationMapper.mapFromRegistrationRequestAndAuthResponse(
                    request, authRegistrationResponse
            );

            log.info("Saving organization: {}", organization.toString());

            Organization saved = organizationRepository.save(organization);

            log.info("Saving successful - organization id: {}", saved.getId());

        } catch (Exception e) {

            validateException(e);
            throw e;

        }

    }

}
