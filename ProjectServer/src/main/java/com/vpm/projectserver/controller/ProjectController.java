package com.vpm.projectserver.controller;


import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for handling request purely concerning projects - without any dependencies, like user ID
 * All HTTP methods included
 */

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(
            ProjectService projectService
    ) {
        this.projectService = projectService;
    }


    @GetMapping
    public ResponseEntity<?> getAllProjects() {

        return ResponseEntity
                .ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(
            @PathVariable Long id
    ) {
        return ResponseEntity
                .ok(projectService.getProjectById(id));
    }

}
