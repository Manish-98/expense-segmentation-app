package com.expense.segmentation.controller;

import com.expense.segmentation.config.JwtAuthenticationFilter;
import com.expense.segmentation.config.JwtTokenUtil;
import com.expense.segmentation.dto.CreateDepartmentRequest;
import com.expense.segmentation.dto.DepartmentResponse;
import com.expense.segmentation.dto.UpdateDepartmentRequest;
import com.expense.segmentation.service.CustomUserDetailsService;
import com.expense.segmentation.service.DepartmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private DepartmentResponse departmentResponse;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        departmentResponse = new DepartmentResponse();
        departmentResponse.setId(departmentId);
        departmentResponse.setName("Engineering");
        departmentResponse.setCode("ENG");
        departmentResponse.setManagerId(UUID.randomUUID());
        departmentResponse.setManagerName("Manager User");
        departmentResponse.setCreatedAt(LocalDateTime.now());
        departmentResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDepartment_WithAdminRole_ShouldCreateDepartment() throws Exception {
        // Given
        CreateDepartmentRequest request = new CreateDepartmentRequest("Engineering", "ENG", UUID.randomUUID());
        when(departmentService.createDepartment(any(CreateDepartmentRequest.class))).thenReturn(departmentResponse);

        // When & Then
        mockMvc.perform(post("/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Engineering"))
                .andExpect(jsonPath("$.code").value("ENG"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDepartment_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateDepartmentRequest request = new CreateDepartmentRequest("", "", null);

        // When & Then
        mockMvc.perform(post("/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDepartments_WithAdminRole_ShouldReturnDepartments() throws Exception {
        // Given
        when(departmentService.getAllDepartments()).thenReturn(Arrays.asList(departmentResponse));

        // When & Then
        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Engineering"));
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void getAllDepartments_WithFinanceRole_ShouldReturnDepartments() throws Exception {
        // Given
        when(departmentService.getAllDepartments()).thenReturn(Arrays.asList(departmentResponse));

        // When & Then
        mockMvc.perform(get("/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDepartmentById_WithAdminRole_ShouldReturnDepartment() throws Exception {
        // Given
        when(departmentService.getDepartmentById(departmentId)).thenReturn(departmentResponse);

        // When & Then
        mockMvc.perform(get("/departments/{id}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(departmentId.toString()))
                .andExpect(jsonPath("$.name").value("Engineering"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDepartment_WithAdminRole_ShouldUpdateDepartment() throws Exception {
        // Given
        UpdateDepartmentRequest request = new UpdateDepartmentRequest("Engineering Updated", null);
        departmentResponse.setName("Engineering Updated");
        when(departmentService.updateDepartment(eq(departmentId), any(UpdateDepartmentRequest.class)))
                .thenReturn(departmentResponse);

        // When & Then
        mockMvc.perform(patch("/departments/{id}", departmentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Engineering Updated"));
    }
}
