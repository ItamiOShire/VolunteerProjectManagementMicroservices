package com.vpm.taskserver.controller;


import com.vpm.taskserver.dto.request.AssignVolunteerReportingTaskSuggestionRequest;
import com.vpm.taskserver.dto.request.AssignVolunteerToTaskRequest;
import com.vpm.taskserver.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


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

    @PostMapping("{volunteerId}/tasks")
    public ResponseEntity<?> assignTaskToVolunteer(
            @PathVariable("volunteerId") Long volunteerId,
            @RequestBody AssignVolunteerToTaskRequest request
    ) {

        return ResponseEntity
                .created(URI.create("unavailable"))
                .body(
                        taskService.assignVolunteerToTask(
                                request,
                                volunteerId
                        )
                );
    }

    @PostMapping("{volunteerId}/tasks/suggestions")
    public ResponseEntity<?> assignTaskToVolunteerSuggestion(
            @PathVariable("volunteerId")  Long volunteerId,
            @RequestBody AssignVolunteerReportingTaskSuggestionRequest request
    ) {

        return ResponseEntity
                .created(URI.create("unavailable"))
                .body(
                        taskService.assignVolunteerReportingTaskSuggestion(
                                request,
                                volunteerId
                        )
                );
    }

}
