package com.vpm.projectserver.controller;


import com.vpm.projectserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for handling request about specific organization's project
 * All HTTP methods included
 */

@RestController
@RequestMapping("/api/organizations")
public class OrganizationsProjectsController {

    private final ProjectService projectService;

    @Autowired
    public OrganizationsProjectsController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/{id}/projects")
    public ResponseEntity<?> getProjectsByOrganization(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity
                .ok(projectService.getAllOrganizationProjects(id));
    }

}
