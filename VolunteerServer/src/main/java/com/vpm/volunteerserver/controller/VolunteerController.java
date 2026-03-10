package com.vpm.volunteerserver.controller;


import com.vpm.volunteerserver.dto.request.VolunteerRegisterRequest;
import com.vpm.volunteerserver.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {

    private final RegistrationService registrationService;

    @Autowired
    public VolunteerController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerVolunteer(
            @RequestBody VolunteerRegisterRequest request
            ) {

        registrationService.registerVolunteer(request);

        return ResponseEntity.ok("Volunteer registered successfully");
    }
}
