package com.expense.segmentation.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void getExpenseSegments_WithEmployeeRole_ShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void getExpenseSegments_WithManagerRole_ShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = {"FINANCE"})
    void getExpenseSegments_WithFinanceRole_ShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getExpenseSegments_WithAdminRole_ShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
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
    void getExpenseSegments_WithValidExpenseId_ShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
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
                .andExpect(jsonPath("$.length()").value(0));

        // Test second expense ID
        mockMvc.perform(
                        get("/expenses/{id}/segments", expense2.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addExpenseSegment_WithValidData_ShouldCreateAndReturnSegment() throws Exception {
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
                .andExpect(jsonPath("$[0].percentage").value(50.00))
                .andExpect(jsonPath("$[0].id").exists());

        // Verify the segment was actually saved by retrieving it
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Travel"));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addExpenseSegment_WithPercentageProvided_ShouldUseProvidedPercentage() throws Exception {
        String segmentRequest =
                """
            {
                "category": "Meals",
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
                .andExpect(jsonPath("$[0].percentage").value(35.00));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addExpenseSegment_WithAmountExceedingExpense_ShouldReturnBadRequest() throws Exception {
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
                .andExpect(jsonPath("$.message").exists())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        org.hamcrest.Matchers.containsString(
                                                "exceeds expense amount")));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
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
    @WithMockUser(roles = {"EMPLOYEE"})
    void addExpenseSegment_WithNegativeAmount_ShouldReturnBadRequest() throws Exception {
        String segmentRequest =
                """
            {
                "category": "Travel",
                "amount": -10.00
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
    @WithMockUser(roles = {"EMPLOYEE"})
    void addExpenseSegment_WithZeroAmount_ShouldReturnBadRequest() throws Exception {
        String segmentRequest =
                """
            {
                "category": "Travel",
                "amount": 0.00
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
    @WithMockUser(roles = {"EMPLOYEE"})
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
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addExpenseSegment_WhenSegmentsAlreadyExist_ShouldReturnBadRequest() throws Exception {
        // First, create a segment
        String segmentRequest1 =
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
                                .content(segmentRequest1))
                .andExpect(status().isCreated());

        // Try to add another segment to the same expense
        String segmentRequest2 =
                """
            {
                "category": "Meals",
                "amount": 50.00
            }
            """;

        mockMvc.perform(
                        post("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentRequest2))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        org.hamcrest.Matchers.containsString(
                                                "already has segments")));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addMultipleExpenseSegments_WithValidData_ShouldCreateAllSegments() throws Exception {
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
                                .value(org.hamcrest.Matchers.containsInAnyOrder(40.00, 60.00)))
                .andExpect(
                        jsonPath("$[*].percentage")
                                .value(org.hamcrest.Matchers.containsInAnyOrder(40.00, 60.00)));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
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
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        org.hamcrest.Matchers.containsString(
                                                "must equal expense amount")));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addMultipleExpenseSegments_WithDuplicateCategories_ShouldReturnBadRequest()
            throws Exception {
        String segmentsRequest =
                """
            {
                "segments": [
                    {
                        "category": "Travel",
                        "amount": 40.00
                    },
                    {
                        "category": "Travel",
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
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        org.hamcrest.Matchers.containsString(
                                                "categories must be unique")));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void replaceAllExpenseSegments_WithValidData_ShouldReplaceExistingSegments() throws Exception {
        // First, create a segment
        String segmentRequest1 =
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
                                .content(segmentRequest1))
                .andExpect(status().isCreated());

        // Verify the segment exists
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Travel"));

        // Replace with different segments
        String replaceRequest =
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
                                .content(replaceRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Supplies"))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].percentage").value(100.00));

        // Verify the old segment was replaced
        mockMvc.perform(
                        get("/expenses/{id}/segments", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Supplies"));
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void addExpenseSegment_WithManagerRole_ShouldWork() throws Exception {
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
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"FINANCE"})
    void addExpenseSegment_WithFinanceRole_ShouldWork() throws Exception {
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
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void addExpenseSegment_WithAdminRole_ShouldWork() throws Exception {
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
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addExpenseSegment_WithEmptySegmentsList_ShouldReturnBadRequest() throws Exception {
        String segmentsRequest =
                """
            {
                "segments": []
            }
            """;

        mockMvc.perform(
                        post("/expenses/{id}/segments/batch", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentsRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void addExpenseSegment_WithTooManySegments_ShouldReturnBadRequest() throws Exception {
        StringBuilder segmentsJson = new StringBuilder("{\"segments\": [");
        for (int i = 1; i <= 21; i++) {
            if (i > 1) segmentsJson.append(",");
            segmentsJson.append(
                    String.format(
                            "{\"category\": \"Category%d\", \"amount\": %.2f}", i, 100.0 / 21));
        }
        segmentsJson.append("]}");

        mockMvc.perform(
                        post("/expenses/{id}/segments/batch", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segmentsJson.toString()))
                .andExpect(status().isBadRequest());
    }
}
