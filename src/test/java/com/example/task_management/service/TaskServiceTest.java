
package com.example.task_management.service;

import com.example.task_management.dto.CreateTaskRequest;
import com.example.task_management.dto.TaskResponse;
import com.example.task_management.entity.AppUser;
import com.example.task_management.entity.Task;
import com.example.task_management.enums.TaskStatus;
import com.example.task_management.repository.TaskRepository;
import com.example.task_management.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldCreateTaskSuccessfully() {

        // Arrange
        String email = "test@example.com";

        AppUser owner = mock(AppUser.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(owner));

        CreateTaskRequest request = new CreateTaskRequest(
                "Complete assignment",
                "Finish the Spring Boot task management project",
                LocalDate.of(2026, 12, 1)
        );

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponse response = taskService.createTask(request, email);

        // Assert
        assertNotNull(response);

        assertEquals("Complete assignment", response.title());

        assertEquals(
                "Finish the Spring Boot task management project",
                response.description()
        );

        assertEquals(TaskStatus.TODO, response.status());

        assertEquals(
                LocalDate.of(2026, 12, 1),
                response.dueDate()
        );

        ArgumentCaptor<Task> taskCaptor =
                ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();

        assertEquals("Complete assignment", savedTask.getTitle());

        assertEquals(TaskStatus.TODO, savedTask.getStatus());

        assertSame(owner, savedTask.getOwner());

        verify(userRepository).findByEmail(email);
    }
}
