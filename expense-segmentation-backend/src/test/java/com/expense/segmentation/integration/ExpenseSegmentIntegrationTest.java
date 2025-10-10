package com.expense.segmentation.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expense.segmentation.config.JwtAuthenticationFilter;
import com.expense.segmentation.config.JwtTokenUtil;
import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseStatus;
import com.expense.segmentation.model.ExpenseType;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.RoleRepository;
import com.expense.segmentation.repository.UserRepository;
import com.expense.segmentation.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class ExpenseSegmentIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private ExpenseRepository expenseRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private RoleRepository roleRepository;

    @MockBean private JwtTokenUtil jwtTokenUtil;

    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean private CustomUserDetailsService customUserDetailsService;

    private UUID testExpenseId;

    @BeforeEach
    void setUp() {
        // Clean up data to avoid conflicts
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        // Check if role already exists, if not create it
        Role employeeRole = roleRepository.findByName(RoleType.EMPLOYEE).orElse(null);
        if (employeeRole == null) {
            employeeRole = new Role();
            employeeRole.setName(RoleType.EMPLOYEE);
            employeeRole.setDescription("Employee role");
            employeeRole = roleRepository.save(employeeRole);
        }

        // Create and save user
        User testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test-" + UUID.randomUUID() + "@example.com");
        testUser.setPasswordHash("password");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRole(employeeRole);
        testUser = userRepository.save(testUser);

        // Create and save expense
        Expense testExpense = new Expense();
        testExpense.setDate(LocalDate.now());
        testExpense.setVendor("Test Vendor");
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setDescription("Test Description");
        testExpense.setType(ExpenseType.EXPENSE);
        testExpense.setStatus(ExpenseStatus.SUBMITTED);
        testExpense.setCreatedBy(testUser);
        testExpense = expenseRepository.save(testExpense);
        testExpenseId = testExpense.getId();
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getExpenseSegments_WithEmployeeRole_ShouldReturnSegments() throws Exception {
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[*].category").exists())
                .andExpect(jsonPath("$[*].amount").exists())
                .andExpect(jsonPath("$[*].percentage").exists())
                .andExpect(jsonPath("$[?(@.category == 'Travel')].percentage").value(40.00))
                .andExpect(jsonPath("$[?(@.category == 'Meals')].percentage").value(30.00))
                .andExpect(jsonPath("$[?(@.category == 'Supplies')].percentage").value(20.00))
                .andExpect(jsonPath("$[?(@.category == 'Other')].percentage").value(10.00));
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void getExpenseSegments_WithManagerRole_ShouldReturnSegments() throws Exception {
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @WithMockUser(roles = {"FINANCE"})
    void getExpenseSegments_WithFinanceRole_ShouldReturnSegments() throws Exception {
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getExpenseSegments_WithAdminRole_ShouldReturnSegments() throws Exception {
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getExpenseSegments_WithInvalidExpenseId_ShouldReturnNotFound() throws Exception {
        UUID invalidExpenseId = UUID.randomUUID();

        mockMvc.perform(
                        get("/expenses/{id}/segments", invalidExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getExpenseSegments_WithValidExpenseId_ShouldReturnCorrectJsonStructure() throws Exception {
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].category").isString())
                .andExpect(jsonPath("$[0].amount").isNumber())
                .andExpect(jsonPath("$[0].percentage").isNumber())
                .andExpect(jsonPath("$[0].amount").value(40.00))
                .andExpect(jsonPath("$[0].percentage").value(40.00));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void getExpenseSegments_WithDifferentExpenseIds_ShouldReturnMockDataForEach() throws Exception {
        // Create another expense for testing
        Role employeeRole = roleRepository.findByName(RoleType.EMPLOYEE).orElseThrow();

        User testUser2 = new User();
        testUser2.setName("Test User 2");
        testUser2.setEmail("test2-" + UUID.randomUUID() + "@example.com");
        testUser2.setPasswordHash("password");
        testUser2.setStatus(UserStatus.ACTIVE);
        testUser2.setRole(employeeRole);
        testUser2 = userRepository.save(testUser2);

        Expense expense2 = new Expense();
        expense2.setDate(LocalDate.now());
        expense2.setVendor("Test Vendor 2");
        expense2.setAmount(new BigDecimal("200.00"));
        expense2.setDescription("Test Description 2");
        expense2.setType(ExpenseType.EXPENSE);
        expense2.setStatus(ExpenseStatus.SUBMITTED);
        expense2.setCreatedBy(testUser2);
        expense2 = expenseRepository.save(expense2);

        // Test original expense ID
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));

        // Test second expense ID
        mockMvc.perform(
                        get("/expenses/{id}/segments", expense2.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }
}
