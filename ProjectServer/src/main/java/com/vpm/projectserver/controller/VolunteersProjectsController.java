package com.vpm.projectserver.controller;

import com.vpm.projectserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller responsible for handling request about specific volunteer's project
 * All HTTP methods included
 */

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
            @PathVariable("id") Long id
    ) {
        return ResponseEntity
                .ok(projectService.getAllVolunteerProjects(id));
    }


}
