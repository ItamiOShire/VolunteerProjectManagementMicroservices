package com.vpm.organizationserver.controller;

import com.vpm.organizationserver.dto.request.CreateDescriptionRequest;
import com.vpm.organizationserver.dto.request.OrganizationRegisterRequest;
import com.vpm.organizationserver.dto.request.UpdateDescriptionRequest;
import com.vpm.organizationserver.service.OrganizationService;
import com.vpm.organizationserver.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final RegistrationService registrationService;

    public OrganizationController(
            OrganizationService organizationService,
            RegistrationService registrationService
    ) {
        this.organizationService = organizationService;
        this.registrationService = registrationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrganizationProfile(
            @PathVariable("id") Long OrganizationUserId
    ) {
       return ResponseEntity
               .ok(organizationService.getOrganizationProfile(OrganizationUserId));
    }

    @GetMapping("/{id}/description")
    public ResponseEntity<?> getOrganizationDescription(
            @PathVariable("id") Long organizationUserId
    ) {
        return ResponseEntity
               .ok(organizationService.getOrganizationDescription(organizationUserId));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerOrganization(
            @RequestBody OrganizationRegisterRequest request
    ) {

        registrationService.register(
                request
        );

        return ResponseEntity
                .ok().build();

    }

    @PostMapping("/{id}/description")
    public ResponseEntity<?> createOrganizationDescription(
            @PathVariable("id") Long organizationUserId,
            @RequestBody CreateDescriptionRequest request
    ) {

        organizationService.createOrganizationDescription(
                request,
                organizationUserId
        );

        return ResponseEntity
                .ok().build();
    }

    @PutMapping("/{id}/description")
    public ResponseEntity<?> updateOrganizationDescription(
            @PathVariable("id") Long organizationUserId,
            @RequestBody UpdateDescriptionRequest request
    ) {

        organizationService.updateOrganizationDescription(
                request,
                organizationUserId
        );

        return ResponseEntity
                .ok().build();
    }

    @PatchMapping("/{id}/description")
    public ResponseEntity<?> updateOrganizationDescription(
            @PathVariable("id") Long organizationUserId,
            @RequestBody Map<String, Object> updates
    ) {

        organizationService.patchOrganizationDescription(
                updates,
                organizationUserId
        );

        return ResponseEntity
                .ok().build();
    }

}
