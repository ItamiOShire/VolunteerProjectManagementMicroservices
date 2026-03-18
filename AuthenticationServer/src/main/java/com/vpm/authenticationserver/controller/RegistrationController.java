package com.vpm.authenticationserver.controller;

import com.vpm.authenticationserver.exception.request.InvalidServiceNameHeaderException;
import com.vpm.authenticationserver.exception.request.MissingRequiredHeaderException;
import com.vpm.authenticationserver.service.RegistrationService;
import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/internal/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    /*
     * services allowed for internal API communication with Authentication Server. This is a basic validation mechanism to ensure that only authorized services can access the registration endpoint.
     */
    private final Set<String> allowedServices = new HashSet<>(
            Set.of(
                    "volunteer-service",
                    "organization-service"
            )
    );

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    private void validateInternalRequest(String internalRequestHeader, String serviceNameHeader) {

        if (internalRequestHeader == null || serviceNameHeader == null) {
            List<String> headers = new ArrayList<>();
            if (internalRequestHeader == null) headers.add(internalRequestHeader);
            if (serviceNameHeader == null) headers.add(serviceNameHeader);
            throw new MissingRequiredHeaderException(headers);
        }

        if (!allowedServices.contains(serviceNameHeader.toLowerCase())) {
            throw new InvalidServiceNameHeaderException(serviceNameHeader);
        }
    }

    /*
     * Each request should contain at least 2 required headers to identify if the request is from another microservice
     *  - X-INTERNAL-REQUEST (true/false)
     *  - X-SERVICE-NAME (String)
     */

    @PostMapping
    public AuthRegistrationResponse registerUser(
            @RequestHeader ( value = "X-INTERNAL-REQUEST") String internalRequestHeader,
            @RequestHeader ( value = "X-SERVICE-NAME") String serviceNameHeader,
            @RequestBody AuthRegistrationRequest request
    ) {

        validateInternalRequest(
                internalRequestHeader,
                serviceNameHeader
        );

        return registrationService.registerUserInAuthService(request);

    }

}
