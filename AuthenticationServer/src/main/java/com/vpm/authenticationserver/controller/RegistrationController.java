package com.vpm.authenticationserver.controller;

import com.vpm.authenticationserver.service.RegistrationService;
import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public AuthRegistrationResponse registerUser(
            @RequestBody AuthRegistrationRequest request
    ) {

        return registrationService.registerUserInAuthService(request);

    }

}
