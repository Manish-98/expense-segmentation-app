package com.expense.segmentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expense.segmentation.config.JwtAuthenticationFilter;
import com.expense.segmentation.config.JwtTokenUtil;
import com.expense.segmentation.dto.CreateExpenseRequest;
import com.expense.segmentation.dto.ExpenseResponse;
import com.expense.segmentation.model.ExpenseStatus;
import com.expense.segmentation.model.ExpenseType;
import com.expense.segmentation.service.CustomUserDetailsService;
import com.expense.segmentation.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
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

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private ExpenseService expenseService;

    @MockBean private JwtTokenUtil jwtTokenUtil;

    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean private CustomUserDetailsService customUserDetailsService;

    private ExpenseResponse expenseResponse;
    private UUID expenseId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        userId = UUID.randomUUID();

        expenseResponse = new ExpenseResponse();
        expenseResponse.setId(expenseId);
        expenseResponse.setDate(LocalDate.now());
        expenseResponse.setVendor("Test Vendor");
        expenseResponse.setAmount(new BigDecimal("100.00"));
        expenseResponse.setDescription("Test expense");
        expenseResponse.setType(ExpenseType.EXPENSE);
        expenseResponse.setStatus(ExpenseStatus.SUBMITTED);
        expenseResponse.setCreatedById(userId);
        expenseResponse.setCreatedByName("Test User");
        expenseResponse.setCreatedAt(LocalDateTime.now());
        expenseResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createExpense_WithEmployeeRole_ShouldCreateExpense() throws Exception {
        // Given
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setDate(LocalDate.now());
        request.setVendor("Test Vendor");
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Test expense");
        request.setType(ExpenseType.EXPENSE);

        when(expenseService.createExpense(any(CreateExpenseRequest.class)))
                .thenReturn(expenseResponse);

        // When & Then
        mockMvc.perform(
                        post("/expenses")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendor").value("Test Vendor"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createExpense_WithManagerRole_ShouldCreateExpense() throws Exception {
        // Given
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setDate(LocalDate.now());
        request.setVendor("Test Vendor");
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Test expense");
        request.setType(ExpenseType.INVOICE);

        expenseResponse.setType(ExpenseType.INVOICE);
        when(expenseService.createExpense(any(CreateExpenseRequest.class)))
                .thenReturn(expenseResponse);

        // When & Then
        mockMvc.perform(
                        post("/expenses")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("INVOICE"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createExpense_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - invalid request with missing required fields
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setVendor(""); // Empty vendor
        request.setAmount(null); // Null amount

        // When & Then
        mockMvc.perform(
                        post("/expenses")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void getAllExpenses_WithFinanceRole_ShouldReturnExpenses() throws Exception {
        // Given
        when(expenseService.getAllExpenses()).thenReturn(Arrays.asList(expenseResponse));

        // When & Then
        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].vendor").value("Test Vendor"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllExpenses_WithAdminRole_ShouldReturnExpenses() throws Exception {
        // Given
        when(expenseService.getAllExpenses()).thenReturn(Arrays.asList(expenseResponse));

        // When & Then
        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getExpenseById_WithEmployeeRole_ShouldReturnExpense() throws Exception {
        // Given
        when(expenseService.getExpenseById(expenseId)).thenReturn(expenseResponse);

        // When & Then
        mockMvc.perform(get("/expenses/{id}", expenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()))
                .andExpect(jsonPath("$.vendor").value("Test Vendor"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getExpenseById_WithManagerRole_ShouldReturnExpense() throws Exception {
        // Given
        when(expenseService.getExpenseById(expenseId)).thenReturn(expenseResponse);

        // When & Then
        mockMvc.perform(get("/expenses/{id}", expenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()));
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void getExpensesByUser_WithFinanceRole_ShouldReturnUserExpenses() throws Exception {
        // Given
        when(expenseService.getExpensesByUser(userId)).thenReturn(Arrays.asList(expenseResponse));

        // When & Then
        mockMvc.perform(get("/expenses/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].createdById").value(userId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getExpensesByUser_WithAdminRole_ShouldReturnUserExpenses() throws Exception {
        // Given
        when(expenseService.getExpensesByUser(userId)).thenReturn(Arrays.asList(expenseResponse));

        // When & Then
        mockMvc.perform(get("/expenses/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getExpensesByUser_WithManagerRole_ShouldReturnUserExpenses() throws Exception {
        // Given
        when(expenseService.getExpensesByUser(userId)).thenReturn(Arrays.asList(expenseResponse));

        // When & Then
        mockMvc.perform(get("/expenses/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getExpensesByUser_WhenServiceThrowsSecurityException_ShouldReturnForbidden()
            throws Exception {
        // Given
        when(expenseService.getExpensesByUser(userId))
                .thenThrow(new SecurityException("You are not authorized to view expenses for this user"));

        // When & Then
        mockMvc.perform(get("/expenses/user/{userId}", userId))
                .andExpect(status().isInternalServerError());
    }
}
