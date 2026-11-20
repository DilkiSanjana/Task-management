package com.example.task_management;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    // Register a new user and return their JWT token
    private String registerUser() throws Exception {
        String email = "task-test-" + UUID.randomUUID() + "@example.com";

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "%s",
                            "password": "password123"
                        }
                        """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.token");
    }

    // Create a task and return its ID
    private Long createTask(String token, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "%s",
                            "description": "Integration test description",
                            "dueDate": "2027-01-15"
                        }
                        """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn();

        return ((Number) JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.id")).longValue();
    }

    @Test
    void shouldCreateTask() throws Exception {
        String token = registerUser();

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Complete assignment",
                            "description": "Finish the integration tests",
                            "dueDate": "2027-01-15"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Complete assignment"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void shouldGetAllTasksForLoggedInUser() throws Exception {
        String token = registerUser();
        createTask(token, "First task");
        createTask(token, "Second task");

        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void shouldGetTaskById() throws Exception {
        String token = registerUser();
        Long taskId = createTask(token, "Task to retrieve");

        mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Task to retrieve"));
    }

    @Test
    void shouldUpdateTask() throws Exception {
        String token = registerUser();
        Long taskId = createTask(token, "Old task title");

        mockMvc.perform(put("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Updated task title",
                            "description": "Updated description",
                            "status": "IN_PROGRESS",
                            "dueDate": "2027-02-20"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated task title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        String token = registerUser();
        Long taskId = createTask(token, "Task to delete");

        mockMvc.perform(delete("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchTasksByKeyword() throws Exception {
        String token = registerUser();
        createTask(token, "Prepare Java assignment");
        createTask(token, "Buy groceries");

        mockMvc.perform(get("/api/tasks/search")
                .header("Authorization", "Bearer " + token)
                .param("keyword", "Java")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Prepare Java assignment"));
    }

    @Test
    void shouldNotAllowUserToAccessAnotherUsersTask() throws Exception {
        String firstUserToken = registerUser();
        String secondUserToken = registerUser();

        Long taskId = createTask(firstUserToken, "Private task");

        mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + secondUserToken))
                .andExpect(status().isNotFound());
    }
}
