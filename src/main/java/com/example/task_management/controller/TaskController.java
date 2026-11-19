package com.example.task_management.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import com.example.task_management.dto.TaskResponse;
import com.example.task_management.enums.TaskStatus;
import com.example.task_management.service.TaskService;
import com.example.task_management.dto.createTaskRequest;
import com.example.task_management.dto.updateTaskRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
   private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Create task
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody createTaskRequest request,
            Authentication authentication) {

        TaskResponse response = taskService.createTask(
                request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all tasks with optional status filter and pagination
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable,
            Authentication authentication) {

        return ResponseEntity.ok(
                taskService.getTasks(
                        authentication.getName(),
                        status,
                        pageable));
    }

    // Get task by ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                taskService.getTask(
                        id, authentication.getName()));
    }

    // Update task
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody updateTaskRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                taskService.updateTask(
                        id, request, authentication.getName()));
    }

    // Delete task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Authentication authentication) {

        taskService.deleteTask(
                id, authentication.getName());

        return ResponseEntity.noContent().build();
    }
 
}
