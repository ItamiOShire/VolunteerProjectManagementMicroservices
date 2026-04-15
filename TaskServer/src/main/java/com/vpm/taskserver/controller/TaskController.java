package com.vpm.taskserver.controller;

import com.vpm.taskserver.dto.request.CreateTaskRequest;
import com.vpm.taskserver.dto.request.UpdateTaskRequest;
import com.vpm.taskserver.dto.template.TaskTemplate;
import com.vpm.taskserver.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;


@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(
            TaskService taskService
    ) {
        this.taskService = taskService;
    }

    @GetMapping()
    public ResponseEntity<?> getAllTasks() {

        return ResponseEntity
                .ok(taskService.getAllTasks());
    }

    @PostMapping()
    public ResponseEntity<?> createTask(
            @RequestBody CreateTaskRequest request
    ) {

        TaskTemplate created = taskService.createTask(request);

        return ResponseEntity
                .created(
                        URI.create("/api/tasks/" + created.getItemId())
                )
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(
            @PathVariable("id") Long taskId,
            @RequestBody UpdateTaskRequest request
    ) {

        TaskTemplate updated = taskService.updateTask(
                request,
                taskId
        );

        return ResponseEntity
                .ok(
                        updated
                );

    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchTask(
            @PathVariable("id") Long taskId,
            Map<String, Object> updates
    ) {

        TaskTemplate patched = taskService.patchTask(
                updates,
                taskId
        );

        return ResponseEntity
                .ok(
                        patched
                );

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(
            @PathVariable("id") Long taskId
    ) {

        taskService.deleteTask(taskId);

        return ResponseEntity
                .ok().build();
    }

}
