package com.example.task_management.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.task_management.enums.TaskStatus;

public record TaskResponse(
     Long id,
    String title,
    String description,
    TaskStatus status,
    LocalDate dueDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
