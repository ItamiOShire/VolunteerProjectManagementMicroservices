package com.vpm.taskserver.controller;

import com.vpm.taskserver.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectTaskController {

    private final TaskService taskService;

    @Autowired
    public ProjectTaskController(
            TaskService taskService
    ) {
        this.taskService = taskService;
    }

    @GetMapping("{id}/tasks")
    public ResponseEntity<?> getAllProjectTasks(
            @PathVariable("id") Long projectId
    ) {

        return ResponseEntity
                .ok(
                        taskService.getAllProjectTasks(
                                projectId
                        )
                );

    }
}
