package com.expense.segmentation.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expense.segmentation.config.JwtAuthenticationFilter;
import com.expense.segmentation.config.JwtTokenUtil;
import com.expense.segmentation.model.Category;
import com.expense.segmentation.repository.CategoryRepository;
import com.expense.segmentation.repository.RoleRepository;
import com.expense.segmentation.repository.UserRepository;
import com.expense.segmentation.service.CustomUserDetailsService;
import com.expense.segmentation.service.ExpenseAuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class CategoryIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private CategoryRepository categoryRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private UserRepository userRepository;

    @MockBean private JwtTokenUtil jwtTokenUtil;

    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean private CustomUserDetailsService customUserDetailsService;

    @MockBean private ExpenseAuthorizationService expenseAuthorizationService;

    private Category testCategory;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        // Clean up data to avoid conflicts
        categoryRepository.deleteAll();

        // Create test category
        testCategory = new Category();
        testCategory.setName("Travel");
        testCategory.setDescription("Travel expenses");
        testCategory.setActive(true);
        testCategory = categoryRepository.save(testCategory);
        categoryId = testCategory.getId();

        // Mock authorization service to allow category management
        when(expenseAuthorizationService.canManageCategories(any(String.class))).thenReturn(true);
    }

    @Test
    @WithMockUser(
            username = "manager@example.com",
            roles = {"MANAGER"})
    void getAllCategories_WithActiveCategories_ShouldReturnActiveCategories() throws Exception {
        // Given - create an inactive category
        Category inactiveCategory = new Category();
        inactiveCategory.setName("Inactive");
        inactiveCategory.setDescription("Inactive category");
        inactiveCategory.setActive(false);
        categoryRepository.save(inactiveCategory);

        // When & Then
        mockMvc.perform(get("/categories").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$[0].name").value("Travel"))
                .andExpect(jsonPath("$[0].description").value("Travel expenses"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @WithMockUser(
            username = "manager@example.com",
            roles = {"MANAGER"})
    void getAllCategories_WithNoActiveCategories_ShouldReturnEmptyArray() throws Exception {
        // Given - deactivate the test category
        testCategory.setActive(false);
        categoryRepository.save(testCategory);

        // When & Then
        mockMvc.perform(get("/categories").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(
            username = "manager@example.com",
            roles = {"MANAGER"})
    void createCategory_WithValidData_ShouldCreateAndReturnCategory() throws Exception {
        // Given
        String categoryRequest =
                """
            {
                "name": "Meals",
                "description": "Meal expenses"
            }
            """;

        // When & Then
        mockMvc.perform(
                        post("/categories")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(categoryRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Meals"))
                .andExpect(jsonPath("$.description").value("Meal expenses"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(
            username = "manager@example.com",
            roles = {"MANAGER"})
    void createCategory_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        // Given
        String categoryRequest =
                """
            {
                "name": "Travel",
                "description": "Duplicate travel expenses"
            }
            """;

        // When & Then
        mockMvc.perform(
                        post("/categories")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(categoryRequest))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Resource Already Exists"))
                .andExpect(
                        jsonPath("$.message").value("Category with name 'Travel' already exists"));
    }

    @Test
    @WithMockUser(
            username = "manager@example.com",
            roles = {"MANAGER"})
    void createCategory_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        String categoryRequest =
                """
            {
                "name": "",
                "description": ""
            }
            """;

        // When & Then
        mockMvc.perform(
                        post("/categories")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(categoryRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(
            username = "manager@example.com",
            roles = {"MANAGER"})
    void deactivateCategory_WithExistingCategory_ShouldDeactivateAndReturnCategory()
            throws Exception {
        // When & Then
        mockMvc.perform(
                        delete("/categories/{id}", categoryId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(
            username = "manager@example.com",
            roles = {"MANAGER"})
    void deactivateCategory_WithNonExistentCategory_ShouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(
                        delete("/categories/{id}", nonExistentId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Category not found with ID: " + nonExistentId));
    }

    @Test
    @WithMockUser(
            username = "employee@example.com",
            roles = {"EMPLOYEE"})
    void createCategory_WithEmployeeRole_ShouldReturnForbidden() throws Exception {
        // Given - mock authorization to return false for employee
        when(expenseAuthorizationService.canManageCategories("employee@example.com"))
                .thenReturn(false);

        String categoryRequest =
                """
            {
                "name": "Meals",
                "description": "Meal expenses"
            }
            """;

        // When & Then
        mockMvc.perform(
                        post("/categories")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(categoryRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(
            username = "employee@example.com",
            roles = {"EMPLOYEE"})
    void deactivateCategory_WithEmployeeRole_ShouldReturnForbidden() throws Exception {
        // Given - mock authorization to return false for employee
        when(expenseAuthorizationService.canManageCategories("employee@example.com"))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(
                        delete("/categories/{id}", categoryId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(
            username = "finance@example.com",
            roles = {"FINANCE"})
    void createCategory_WithFinanceRole_ShouldReturnCreated() throws Exception {
        // Given
        String categoryRequest =
                """
            {
                "name": "Supplies",
                "description": "Office supplies"
            }
            """;

        // When & Then
        mockMvc.perform(
                        post("/categories")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(categoryRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Supplies"))
                .andExpect(jsonPath("$.description").value("Office supplies"));
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = {"ADMIN"})
    void deactivateCategory_WithAdminRole_ShouldReturnOk() throws Exception {
        // When & Then
        mockMvc.perform(
                        delete("/categories/{id}", categoryId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
