package com.expense.segmentation.integration;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.expense.segmentation.dto.LoginRequest;
import com.expense.segmentation.dto.RegisterRequest;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        // Ensure EMPLOYEE role exists
        if (!roleRepository.existsByName(RoleType.EMPLOYEE)) {
            Role employeeRole = new Role();
            employeeRole.setName(RoleType.EMPLOYEE);
            employeeRole.setDescription("Employee role");
            roleRepository.save(employeeRole);
        }
    }

    @Test
    void fullAuthFlow_RegisterLoginAndAccessProtectedEndpoint_ShouldSucceed() throws Exception {
        // Step 1: Register a new user
        RegisterRequest registerRequest =
                new RegisterRequest("Integration Test User", "integration@test.com", "password123");

        MvcResult registerResult =
                mockMvc.perform(
                                post("/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(registerRequest)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.token").value(notNullValue()))
                        .andExpect(jsonPath("$.type").value("Bearer"))
                        .andExpect(jsonPath("$.user.email").value("integration@test.com"))
                        .andExpect(jsonPath("$.user.role").value("EMPLOYEE"))
                        .andReturn();

        String registerResponseBody = registerResult.getResponse().getContentAsString();
        String tokenFromRegister =
                objectMapper.readTree(registerResponseBody).get("token").asText();

        // Step 2: Access protected endpoint with token from registration
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + tokenFromRegister))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.name").value("Integration Test User"));

        // Step 3: Login with same credentials
        LoginRequest loginRequest = new LoginRequest("integration@test.com", "password123");

        MvcResult loginResult =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").value(notNullValue()))
                        .andExpect(jsonPath("$.user.email").value("integration@test.com"))
                        .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        String tokenFromLogin = objectMapper.readTree(loginResponseBody).get("token").asText();

        // Step 4: Access protected endpoint with token from login
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + tokenFromLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@test.com"));
    }

    @Test
    void register_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {
        // Step 1: Register first user
        RegisterRequest firstRequest =
                new RegisterRequest("First User", "duplicate@test.com", "password123");

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Step 2: Try to register with same email
        RegisterRequest duplicateRequest =
                new RegisterRequest("Second User", "duplicate@test.com", "password456");

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithWrongPassword_ShouldReturnUnauthorized() throws Exception {
        // Step 1: Register user
        RegisterRequest registerRequest =
                new RegisterRequest("Test User", "wrongpassword@test.com", "correctpassword");

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Step 2: Try to login with wrong password
        LoginRequest loginRequest = new LoginRequest("wrongpassword@test.com", "wrongpassword");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthCheck_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void register_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        RegisterRequest invalidRequest =
                new RegisterRequest("Test User", "invalid-email", "password123");

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        RegisterRequest invalidRequest =
                new RegisterRequest("Test User", "test@example.com", "12345");

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
