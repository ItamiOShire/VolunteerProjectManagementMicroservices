package com.vpm.volunteerserver.controller;

import com.vpm.volunteerserver.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class VolunteersTasksController {

    private final VolunteerService volunteerService;

    @Autowired
    public VolunteersTasksController(
            VolunteerService volunteerService
    ) {
        this.volunteerService = volunteerService;
    }

    /*
     * GET methods
     */

    @GetMapping("{taskId}/projects/{projectId}/volunteers/assigned")
    public ResponseEntity<?> getVolunteersAssignedToTask(
            @PathVariable("taskId") Long taskId,
            @PathVariable("projectId") Long projectId
    ) {
        return ResponseEntity
                .ok(
                        volunteerService.getVolunteersInProjectWhoAreAssignedToTask(
                                taskId,
                                projectId
                        )
                );
    }

    @GetMapping("{taskId}/projects/{projectId}/volunteers")
    public ResponseEntity<?> getVolunteersInProjectByTask(
            @PathVariable("taskId") Long taskId,
            @PathVariable("projectId") Long projectId
    ) {
        return ResponseEntity
                .ok(
                        volunteerService.getVolunteersNotAssignedToTaskWithTaskSuggestion(
                                taskId,
                                projectId
                        )
                );
    }

    //TODO: consider making endpoint for all volunteers in all tasks in given project and then it would be filtered in frontend, reducing requests quantity

}
