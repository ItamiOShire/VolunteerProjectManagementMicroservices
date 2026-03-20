package com.vpm.authenticationserver.controller;

import com.vpm.authenticationserver.service.RegistrationService;
import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
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

        return registrationService.registerUserInAuthService(request);

    }

}
