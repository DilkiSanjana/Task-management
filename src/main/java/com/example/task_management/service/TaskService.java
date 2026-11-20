
package com.example.task_management.service;

import com.example.task_management.dto.TaskResponse;
import com.example.task_management.entity.AppUser;
import com.example.task_management.entity.Task;
import com.example.task_management.enums.TaskStatus;
import com.example.task_management.exception.ResourceNotFoundException;
import com.example.task_management.repository.TaskRepository;
import com.example.task_management.repository.UserRepository;
import com.example.task_management.dto.CreateTaskRequest;
import com.example.task_management.dto.UpdateTaskRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // Create a new task
    public TaskResponse createTask(
            CreateTaskRequest request, String email) {

        AppUser owner = getUser(email);

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setStatus(TaskStatus.TODO);
        task.setOwner(owner);

        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask);
    }

    // Get all tasks belonging to a user
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(
            String email,
            TaskStatus status,
            Pageable pageable) {

        AppUser owner = getUser(email);

        Page<Task> tasks;

        if (status == null) {
            tasks = taskRepository.findAllByOwnerId(
                    owner.getId(), pageable);
        } else {
            tasks = taskRepository.findAllByOwnerIdAndStatus(
                    owner.getId(), status, pageable);
        }

        return tasks.map(this::toResponse);
    }

    // Get one task belonging to a user
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId, String email) {

        AppUser owner = getUser(email);

        Task task = findOwnedTask(taskId, owner.getId());

        return toResponse(task);
    }

    // Update an existing task
    public TaskResponse updateTask(
            Long taskId,
            UpdateTaskRequest request,
            String email) {

        AppUser owner = getUser(email);

        Task task = findOwnedTask(taskId, owner.getId());

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());

        if (request.status() != null) {
            task.setStatus(request.status());
        }

        Task updatedTask = taskRepository.save(task);

        return toResponse(updatedTask);
    }

    // Search tasks by keyword and order by due date
    public Page<TaskResponse> searchTasks(
            String email,
            String keyword,
            String sortDir,
            Pageable pageable) {

        AppUser owner = getUser(email);

        // Determine sorting on dueDate
        org.springframework.data.domain.Sort sort =
                org.springframework.data.domain.Sort.by("dueDate");

        if ("desc".equalsIgnoreCase(sortDir)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        // Preserve original page number and size
        org.springframework.data.domain.PageRequest pageRequest =
                org.springframework.data.domain.PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        sort);

        Page<Task> page = taskRepository.findByOwnerIdAndKeyword(
                owner.getId(), keyword, pageRequest);

        return page.map(this::toResponse);
    }

    // Find user by email
    private AppUser getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));
    }

    // Find task only if it belongs to the logged-in user
    private Task findOwnedTask(Long taskId, Long ownerId) {
        return taskRepository.findByIdAndOwnerId(
                taskId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found"));
    }

    // Convert Task entity into TaskResponse DTO
    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    // Delete a task belonging to the logged-in user
    public void deleteTask(Long id, String email) {

        AppUser owner = getUser(email);

        Task task = findOwnedTask(id, owner.getId());

        taskRepository.delete(task);
    }

    // Get all tasks (admin only)
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {

        Page<Task> tasks = taskRepository.findAll(pageable);

        return tasks.map(this::toResponse);
    }
}
