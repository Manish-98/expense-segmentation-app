package com.expense.segmentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.expense.segmentation.config.JwtAuthenticationFilter;
import com.expense.segmentation.config.JwtTokenUtil;
import com.expense.segmentation.dto.AuthResponse;
import com.expense.segmentation.dto.LoginRequest;
import com.expense.segmentation.dto.RegisterRequest;
import com.expense.segmentation.dto.UserResponse;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.UserStatus;
import com.expense.segmentation.service.AuthService;
import com.expense.segmentation.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;

    @MockBean private JwtTokenUtil jwtTokenUtil;

    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean private CustomUserDetailsService customUserDetailsService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("John Doe", "john@example.com", "password123");
        loginRequest = new LoginRequest("john@example.com", "password123");

        userResponse = new UserResponse();
        userResponse.setId(UUID.randomUUID());
        userResponse.setName("John Doe");
        userResponse.setEmail("john@example.com");
        userResponse.setRole(RoleType.EMPLOYEE);
        userResponse.setStatus(UserStatus.ACTIVE);
        userResponse.setCreatedAt(LocalDateTime.now());
        userResponse.setUpdatedAt(LocalDateTime.now());

        authResponse = new AuthResponse("test-jwt-token", userResponse);
    }

    @Test
    void register_WithValidRequest_ShouldReturnCreated() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"))
                .andExpect(jsonPath("$.user.name").value("John Doe"))
                .andExpect(jsonPath("$.user.role").value("EMPLOYEE"));
    }

    @Test
    void register_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        RegisterRequest invalidRequest =
                new RegisterRequest("John", "invalid-email", "password123");

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("John", "john@example.com", "12345");

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithBlankName_ShouldReturnBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "john@example.com", "password123");

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnOk() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(
                        post("/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    void login_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("invalid-email", "password123");

        mockMvc.perform(
                        post("/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void getCurrentUser_WithAuthentication_ShouldReturnUser() throws Exception {
        when(authService.getCurrentUser()).thenReturn(userResponse);

        mockMvc.perform(get("/auth/me").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }
}
