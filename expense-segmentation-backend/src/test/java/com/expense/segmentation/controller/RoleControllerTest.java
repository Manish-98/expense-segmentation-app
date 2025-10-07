package com.expense.segmentation.controller;

import com.expense.segmentation.config.JwtAuthenticationFilter;
import com.expense.segmentation.config.JwtTokenUtil;
import com.expense.segmentation.dto.RoleResponse;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.service.CustomUserDetailsService;
import com.expense.segmentation.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private RoleResponse employeeRole;
    private RoleResponse adminRole;

    @BeforeEach
    void setUp() {
        employeeRole = new RoleResponse(UUID.randomUUID(), RoleType.EMPLOYEE, "Employee role");
        adminRole = new RoleResponse(UUID.randomUUID(), RoleType.ADMIN, "Admin role");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllRoles_WithAdminRole_ShouldReturnRoles() throws Exception {
        // Given
        when(roleService.getAllRoles()).thenReturn(Arrays.asList(employeeRole, adminRole));

        // When & Then
        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("EMPLOYEE"))
                .andExpect(jsonPath("$[1].name").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void getAllRoles_WithFinanceRole_ShouldReturnRoles() throws Exception {
        // Given
        when(roleService.getAllRoles()).thenReturn(Arrays.asList(employeeRole, adminRole));

        // When & Then
        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
