package com.vpm.projectserver.controller;

import com.vpm.projectserver.dto.request.AssignVolunteerToProjectRequest;
import com.vpm.projectserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/volunteers")
public class VolunteersProjectsController {

    private final ProjectService  projectService;

    @Autowired
    public VolunteersProjectsController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/{id}/projects")
    public ResponseEntity<?> getAllProjectsByVolunteer(
            @PathVariable Long id
    ) {
        return ResponseEntity
                .ok(projectService.getAllVolunteerProjects(id));
    }

    @PostMapping("/{id}/projects")
    public ResponseEntity<?> assignVolunteerToProject(
            @PathVariable("id") Long volunteerId,
            @Validated @RequestBody AssignVolunteerToProjectRequest request
    ) {
        return ResponseEntity
                .ok(projectService.assignVolunteerToProject(request.getProjectId(), volunteerId));
    }

}
