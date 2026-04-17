package com.vpm.volunteerserver.controller;

import com.vpm.volunteerserver.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class VolunteersProjectController {

    private final VolunteerService volunteerService;

    @Autowired
    public VolunteersProjectController(
            VolunteerService volunteerService
    ) {
        this.volunteerService = volunteerService;
    }

    /*
     * GET methods
     */

    @GetMapping("{projectId}/volunteers")
    public ResponseEntity<?> getVolunteersInProject(
            @PathVariable Long projectId
    ) {

        return ResponseEntity
                .ok(volunteerService.getVolunteersInProject(projectId));

    }

}
