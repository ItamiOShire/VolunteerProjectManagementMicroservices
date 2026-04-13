package com.vpm.volunteerserver.controller;


import com.vpm.volunteerserver.dto.request.VolunteerRegisterRequest;
import com.vpm.volunteerserver.service.RegistrationService;
import com.vpm.volunteerserver.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {

    private final RegistrationService registrationService;
    private final VolunteerService  volunteerService;

    @Autowired
    public VolunteerController(
            RegistrationService registrationService,
            VolunteerService volunteerService) {
        this.registrationService = registrationService;
        this.volunteerService = volunteerService;
    }

    // GET HTTP methods

    @GetMapping("/{id}")
    public ResponseEntity<?> getVolunteerProfile(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity
                .ok(
                        volunteerService.getVolunteerProfile(id)
                );

    }

    // POST HTTP methods

    @PostMapping("/register")
    public ResponseEntity<?> registerVolunteer(
            @RequestBody VolunteerRegisterRequest request
            ) {

        registrationService.registerVolunteer(request);

        return ResponseEntity.ok("Volunteer registered successfully");
    }

    // PUT / PATCH HTTP methods

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchVolunteerProfile(
            @PathVariable("id") Long id,
            Map<String, Object> updates
    ) {

        volunteerService.patchVolunteerProfile(
                updates,
                id
        );

        return ResponseEntity
                .ok().build();

    }

}
