package com.expense.segmentation.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class ExpenseIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private ExpenseRepository expenseRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private UserRepository userRepository;

    @MockBean private JwtTokenUtil jwtTokenUtil;

    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean private CustomUserDetailsService customUserDetailsService;

    @MockBean private ExpenseAuthorizationService expenseAuthorizationService;

    private User testUser;
    private Expense testExpense;
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

        // Create and save users for different roles
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("password");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRole(employeeRole);
        testUser = userRepository.save(testUser);

        // Create manager user
        Role managerRole = roleRepository.findByName(RoleType.MANAGER).orElse(null);
        if (managerRole == null) {
            managerRole = new Role();
            managerRole.setName(RoleType.MANAGER);
            managerRole.setDescription("Manager role");
            managerRole = roleRepository.save(managerRole);
        }

        User managerUser = new User();
        managerUser.setName("Manager User");
        managerUser.setEmail("manager@example.com");
        managerUser.setPasswordHash("password");
        managerUser.setStatus(UserStatus.ACTIVE);
        managerUser.setRole(managerRole);
        userRepository.save(managerUser);

        // Create finance user
        Role financeRole = roleRepository.findByName(RoleType.FINANCE).orElse(null);
        if (financeRole == null) {
            financeRole = new Role();
            financeRole.setName(RoleType.FINANCE);
            financeRole.setDescription("Finance role");
            financeRole = roleRepository.save(financeRole);
        }

        User financeUser = new User();
        financeUser.setName("Finance User");
        financeUser.setEmail("finance@example.com");
        financeUser.setPasswordHash("password");
        financeUser.setStatus(UserStatus.ACTIVE);
        financeUser.setRole(financeRole);
        userRepository.save(financeUser);

        // Create admin user
        Role adminRole = roleRepository.findByName(RoleType.ADMIN).orElse(null);
        if (adminRole == null) {
            adminRole = new Role();
            adminRole.setName(RoleType.ADMIN);
            adminRole.setDescription("Admin role");
            adminRole = roleRepository.save(adminRole);
        }

        User adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("password");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setRole(adminRole);
        userRepository.save(adminUser);

        // Create and save expense
        testExpense = new Expense();
        testExpense.setDate(LocalDate.now());
        testExpense.setVendor("Test Vendor");
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setDescription("Test Description");
        testExpense.setType(ExpenseType.EXPENSE);
        testExpense.setStatus(ExpenseStatus.SUBMITTED);
        testExpense.setCreatedBy(testUser);
        testExpense = expenseRepository.save(testExpense);
        testExpenseId = testExpense.getId();

        // Mock authorization service to allow viewing
        when(expenseAuthorizationService.canViewExpense(any(UUID.class), any(String.class)))
                .thenReturn(true);

        // Mock custom user details service to return our test users
        when(customUserDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(
                        org.springframework.security.core.userdetails.User.builder()
                                .username(testUser.getEmail())
                                .password("password")
                                .authorities("ROLE_EMPLOYEE")
                                .build());

        when(customUserDetailsService.loadUserByUsername("manager@example.com"))
                .thenReturn(
                        org.springframework.security.core.userdetails.User.builder()
                                .username("manager@example.com")
                                .password("password")
                                .authorities("ROLE_MANAGER")
                                .build());

        when(customUserDetailsService.loadUserByUsername("finance@example.com"))
                .thenReturn(
                        org.springframework.security.core.userdetails.User.builder()
                                .username("finance@example.com")
                                .password("password")
                                .authorities("ROLE_FINANCE")
                                .build());

        when(customUserDetailsService.loadUserByUsername("admin@example.com"))
                .thenReturn(
                        org.springframework.security.core.userdetails.User.builder()
                                .username("admin@example.com")
                                .password("password")
                                .authorities("ROLE_ADMIN")
                                .build());
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void getExpense_WithExistingExpense_ShouldReturnExpense() throws Exception {
        // When & Then
        mockMvc.perform(
                        get("/expenses/{id}", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testExpenseId.toString()))
                .andExpect(jsonPath("$.vendor").value("Test Vendor"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void getExpense_WithNonExistentExpense_ShouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(
                        get("/expenses/{id}", nonExistentId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void getExpenses_WithDefaultParameters_ShouldReturnPagedExpenses() throws Exception {
        // When & Then
        mockMvc.perform(get("/expenses").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expenses").isArray())
                .andExpect(jsonPath("$.expenses.length()").value(1))
                .andExpect(jsonPath("$.expenses[0].id").value(testExpenseId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void getExpenses_WithCustomPageAndSize_ShouldReturnPagedExpenses() throws Exception {
        // When & Then
        mockMvc.perform(
                        get("/expenses?page=0&size=5")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expenses").isArray())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void getExpenses_WithStatusFilter_ShouldReturnFilteredExpenses() throws Exception {
        // When & Then
        mockMvc.perform(
                        get("/expenses?status=SUBMITTED")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expenses").isArray())
                .andExpect(jsonPath("$.expenses[0].status").value("SUBMITTED"));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void getExpenses_WithTypeFilter_ShouldReturnFilteredExpenses() throws Exception {
        // When & Then
        mockMvc.perform(
                        get("/expenses?type=EXPENSE")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expenses").isArray())
                .andExpect(jsonPath("$.expenses[0].type").value("EXPENSE"));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void getExpenses_WithDateRangeFilter_ShouldReturnFilteredExpenses() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        // When & Then
        mockMvc.perform(
                        get("/expenses?startDate=" + todayStr + "&endDate=" + todayStr)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expenses").isArray())
                .andExpect(jsonPath("$.expenses.length()").value(1));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void createExpense_WithValidData_ShouldCreateAndReturnExpense() throws Exception {
        // Given
        String expenseRequest =
                """
            {
                "date": "%s",
                "vendor": "New Vendor",
                "amount": 250.00,
                "description": "New expense description",
                "type": "EXPENSE"
            }
            """
                        .formatted(LocalDate.now());

        // When & Then
        mockMvc.perform(
                        post("/expenses")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.vendor").value("New Vendor"))
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.description").value("New expense description"))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void createExpense_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        String expenseRequest =
                """
            {
                "date": "invalid-date",
                "vendor": "",
                "amount": -100.00,
                "description": "",
                "type": "INVALID_TYPE"
            }
            """;

        // When & Then
        mockMvc.perform(
                        post("/expenses")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(expenseRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(
            username = "test@example.com",
            roles = {"EMPLOYEE"})
    void getExpenses_WithUnauthorizedAccess_ShouldReturnOwnExpenses() throws Exception {
        // When & Then - Employee should see their own expenses (not forbidden)
        mockMvc.perform(get("/expenses").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expenses").isArray())
                .andExpect(jsonPath("$.expenses.length()").value(1))
                .andExpect(jsonPath("$.expenses[0].id").value(testExpenseId.toString()));
    }

    @Test
    @WithMockUser(
            username = "manager@example.com",
            roles = {"MANAGER"})
    void getExpenses_WithManagerRole_ShouldReturnOwnExpenses() throws Exception {
        // When & Then - Manager should see their own expenses only (like employees)
        mockMvc.perform(get("/expenses").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expenses").isArray())
                .andExpect(jsonPath("$.expenses.length()").value(0)); // Manager has no expenses
    }

    @Test
    @WithMockUser(
            username = "finance@example.com",
            roles = {"FINANCE"})
    void getExpense_WithFinanceRole_ShouldReturnExpense() throws Exception {
        // When & Then
        mockMvc.perform(
                        get("/expenses/{id}", testExpenseId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testExpenseId.toString()));
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = {"ADMIN"})
    void getExpenses_WithAdminRole_ShouldReturnAllExpenses() throws Exception {
        // When & Then
        mockMvc.perform(get("/expenses").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expenses").isArray());
    }
}
