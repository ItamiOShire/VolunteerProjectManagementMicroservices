package com.vpm.projectserver.controller;

import com.vpm.projectserver.dto.request.AssignVolunteerToProjectRequest;
import com.vpm.projectserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class VolunteersProjectsController {

    private final ProjectService  projectService;

    @Autowired
    public VolunteersProjectsController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("volunteers/{id}/projects")
    public ResponseEntity<?> getAllProjectsByVolunteer(
            @PathVariable Long id
    ) {
        return ResponseEntity
                .ok(projectService.getAllVolunteerProjects(id));
    }

    @PostMapping("volunteers/{id}/projects")
    public ResponseEntity<?> assignVolunteerToProject(
            @PathVariable("id") Long volunteerId,
            @Validated @RequestBody AssignVolunteerToProjectRequest request
    ) {
        return ResponseEntity
                .ok(projectService.assignVolunteerToProject(request.getProjectId(), volunteerId));
    }

    @GetMapping("internal/volunteers/{volunteerId}/projects/{projectId}")
    public Boolean getVolunteerProject(
            @RequestHeader("X-INTERNAL-REQUEST") Boolean isInternal,
            @RequestHeader("X-SERVICE-NAME") String serviceName,
            @PathVariable("volunteerId") Long volunteerId,
            @PathVariable("projectId") Long projectId
    ) {
        return projectService.isVolunteerAssignedToProject(
                volunteerId,
                projectId
        );
    }

}
