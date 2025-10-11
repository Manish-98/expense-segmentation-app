package com.expense.segmentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.expense.segmentation.service.ExpenseAuthorizationService;
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
class ExpenseControllerSegmentTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private ExpenseRepository expenseRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private RoleRepository roleRepository;

    @MockBean private JwtTokenUtil jwtTokenUtil;

    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean private ExpenseAuthorizationService expenseAuthorizationService;

    private UUID testExpenseId;
    private String testUserEmail;

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
        testUserEmail = "test-" + UUID.randomUUID() + "@example.com";
        testUser.setEmail(testUserEmail);
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

        // Mock authorization service to allow modification for any expense ID
        // This simplifies testing - authorization is tested separately in integration tests
        when(expenseAuthorizationService.canModifySegments(any(UUID.class), anyString()))
                .thenReturn(true);
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void addExpenseSegment_WithValidData_ShouldReturnCreated() throws Exception {
        String segmentRequest =
                """
            {
                "category": "Travel",
                "amount": 50.00
            }
            """;

        mockMvc.perform(
                        post("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Travel"))
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[0].percentage").value(50.00));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void addExpenseSegment_WithInvalidAmount_ShouldReturnBadRequest() throws Exception {
        String segmentRequest =
                """
            {
                "category": "Travel",
                "amount": 150.00
            }
            """;

        mockMvc.perform(
                        post("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void addExpenseSegment_WithMissingCategory_ShouldReturnBadRequest() throws Exception {
        String segmentRequest =
                """
            {
                "amount": 50.00
            }
            """;

        mockMvc.perform(
                        post("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void addMultipleExpenseSegments_WithValidData_ShouldReturnCreated() throws Exception {
        String segmentsRequest =
                """
            {
                "segments": [
                    {
                        "category": "Travel",
                        "amount": 40.00
                    },
                    {
                        "category": "Meals",
                        "amount": 60.00
                    }
                ]
            }
            """;

        mockMvc.perform(
                        post("/expenses/{id}/segments/batch", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentsRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$[*].category")
                                .value(org.hamcrest.Matchers.containsInAnyOrder("Travel", "Meals")))
                .andExpect(
                        jsonPath("$[*].amount")
                                .value(org.hamcrest.Matchers.containsInAnyOrder(40.00, 60.00)));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void addMultipleExpenseSegments_WithIncorrectTotal_ShouldReturnBadRequest() throws Exception {
        String segmentsRequest =
                """
            {
                "segments": [
                    {
                        "category": "Travel",
                        "amount": 40.00
                    },
                    {
                        "category": "Meals",
                        "amount": 70.00
                    }
                ]
            }
            """;

        mockMvc.perform(
                        post("/expenses/{id}/segments/batch", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentsRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void replaceAllExpenseSegments_WithValidData_ShouldReturnOk() throws Exception {
        String segmentsRequest =
                """
            {
                "segments": [
                    {
                        "category": "Supplies",
                        "amount": 100.00
                    }
                ]
            }
            """;

        mockMvc.perform(
                        put("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentsRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Supplies"))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].percentage").value(100.00));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void addExpenseSegment_WithInvalidExpenseId_ShouldReturnNotFound() throws Exception {
        UUID invalidExpenseId = UUID.randomUUID();
        String segmentRequest =
                """
            {
                "category": "Travel",
                "amount": 50.00
            }
            """;

        mockMvc.perform(
                        post("/expenses/{id}/segments", invalidExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentRequest))
                .andExpect(status().is(404)); // Will be 404 since authorization passes but expense
        // doesn't exist
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void addExpenseSegment_WithPercentageProvided_ShouldUseProvidedPercentage() throws Exception {
        String segmentRequest =
                """
            {
                "category": "Travel",
                "amount": 30.00,
                "percentage": 35.00
            }
            """;

        mockMvc.perform(
                        post("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].percentage").value(35.00));
    }
}
