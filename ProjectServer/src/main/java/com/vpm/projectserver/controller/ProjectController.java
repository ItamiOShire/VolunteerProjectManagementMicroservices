package com.vpm.projectserver.controller;


import com.vpm.projectserver.dto.ProjectTemplate;
import com.vpm.projectserver.dto.request.CreateProjectRequest;
import com.vpm.projectserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

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

    @PostMapping()
    public ResponseEntity<?> createProject(
            @Validated @RequestBody CreateProjectRequest request
    ) {

        ProjectTemplate created = projectService.createProject(request);

        return ResponseEntity
                .created(URI.create("/api/projects/" + created.getItemId()))
                .body(created);
    }

    // TODO: consider making another dto for updating or patching project

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @Validated @RequestBody ProjectTemplate projectTemplate
    ) {

        ProjectTemplate updated = projectService.updateProject(projectTemplate);

        return ResponseEntity
                .ok(updated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @PathVariable("id") Long projectId,
            @RequestBody Map<String, Object> updates
    ) {

        ProjectTemplate patched = projectService.patchProject(
                updates,
                projectId
        );

        return ResponseEntity
                .ok(patched);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(
            @PathVariable("id") Long projectId
    ) {

        projectService.deleteProject(projectId);

        return ResponseEntity
                .ok().build();
    }

}
