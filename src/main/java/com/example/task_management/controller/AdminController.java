package com.example.task_management.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.example.task_management.dto.TaskResponse;
import com.example.task_management.service.TaskService;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final TaskService taskService;

    public AdminController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable) {

        return ResponseEntity.ok(taskService.getAllTasks(pageable));
    }
}