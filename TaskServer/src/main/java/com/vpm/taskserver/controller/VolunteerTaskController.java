package com.vpm.taskserver.controller;


import com.vpm.taskserver.entity.Task;
import com.vpm.taskserver.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerTaskController {

    private final TaskService taskService;

    @Autowired
    public VolunteerTaskController(
            TaskService taskService
    ) {
        this.taskService = taskService;
    }

    // GET HTTP methods

    @GetMapping("{id}/tasks")
    public ResponseEntity<?> getAllVolunteerTasks(
            @PathVariable("id") Long volunteerId
    ) {

        return ResponseEntity
                .ok(
                        taskService.getAllVolunteerTasks(volunteerId)
                );

    }

    @GetMapping("{volunteerId}/projects/{projectId}/tasks")
    public ResponseEntity<?> getAllVolunteerTasksInProject(
            @PathVariable("volunteerId") Long volunteerId,
            @PathVariable("projectId") Long projectId
    ) {

        return ResponseEntity
                .ok(
                        taskService.getAllVolunteerTasksInProject(
                                volunteerId,
                                projectId
                        )
                );

    }

    @GetMapping("{volunteerId}/projects/{projectId}/tasks/suggestions")
    public ResponseEntity<?> getAllVolunteerTasksSuggestionsInProject(
            @PathVariable("volunteerId") Long volunteerId,
            @PathVariable("projectId") Long projectId
    ) {

        return ResponseEntity
                .ok(
                        taskService.getTaskSuggestionsInProjectByVolunteerId(
                                projectId,
                                volunteerId
                        )
                );

    }

}
