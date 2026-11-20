package com.example.task_management;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {
   
    @Autowired
    private MockMvc mockMvc;

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }

    @Test
    void shouldRegisterNewUser() throws Exception {

        String email = uniqueEmail();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "%s",
                            "password": "password123"
                        }
                        """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {

        String email = uniqueEmail();

        String request = """
                {
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldLoginWithCorrectCredentials() throws Exception {

        String email = uniqueEmail();

        String registerRequest = """
                {
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
                {
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldRejectLoginWithIncorrectPassword() throws Exception {

        String email = uniqueEmail();

        String registerRequest = """
                {
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
                {
                    "email": "%s",
                    "password": "wrongpassword"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLoginWithUnknownEmail() throws Exception {

        String loginRequest = """
                {
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(uniqueEmail());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }
}
