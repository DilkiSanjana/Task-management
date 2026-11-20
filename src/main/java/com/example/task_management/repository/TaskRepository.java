package com.example.task_management.repository;

import com.example.task_management.entity.Task;
import com.example.task_management.enums.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, TaskRepositoryCustom {

    Page<Task> findAllByOwnerId(
        Long ownerId,
        Pageable pageable
    );

    Page<Task> findAllByOwnerIdAndStatus(
        Long ownerId,
        TaskStatus status,
        Pageable pageable
    );

    Optional<Task> findByIdAndOwnerId(
        Long taskId,
        Long ownerId
    );
}