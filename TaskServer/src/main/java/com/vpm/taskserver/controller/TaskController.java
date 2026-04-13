package com.vpm.taskserver.controller;

import com.vpm.taskserver.dto.request.CreateTaskRequest;
import com.vpm.taskserver.dto.request.UpdateTaskRequest;
import com.vpm.taskserver.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        taskService.createTask(request);

        return ResponseEntity
                .ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(
            @PathVariable("id") Long taskId,
            @RequestBody UpdateTaskRequest request
    ) {

        taskService.updateTask(
                request,
                taskId
        );

        return ResponseEntity
                .ok().build();

    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchTask(
            @PathVariable("id") Long taskId,
            Map<String, Object> updates
    ) {

        taskService.patchTask(
                updates,
                taskId
        );

        return ResponseEntity
                .ok().build();

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
