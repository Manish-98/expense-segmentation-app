package com.expense.segmentation.integration;

import com.expense.segmentation.dto.CreateDepartmentRequest;
import com.expense.segmentation.dto.RegisterRequest;
import com.expense.segmentation.dto.UpdateDepartmentRequest;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.repository.RoleRepository;
import com.expense.segmentation.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleDepartmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String financeToken;
    private String employeeToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create admin user and get token
        Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        User adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash(passwordEncoder.encode("password123"));
        adminUser.setRole(adminRole);
        userRepository.save(adminUser);

        adminToken = loginAndGetToken("admin@example.com", "password123");

        // Create finance user and get token
        Role financeRole = roleRepository.findByName(RoleType.FINANCE)
                .orElseThrow(() -> new RuntimeException("Finance role not found"));

        User financeUser = new User();
        financeUser.setName("Finance User");
        financeUser.setEmail("finance@example.com");
        financeUser.setPasswordHash(passwordEncoder.encode("password123"));
        financeUser.setRole(financeRole);
        userRepository.save(financeUser);

        financeToken = loginAndGetToken("finance@example.com", "password123");

        // Create employee user and get token
        Role employeeRole = roleRepository.findByName(RoleType.EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("Employee role not found"));

        User employeeUser = new User();
        employeeUser.setName("Employee User");
        employeeUser.setEmail("employee@example.com");
        employeeUser.setPasswordHash(passwordEncoder.encode("password123"));
        employeeUser.setRole(employeeRole);
        userRepository.save(employeeUser);

        employeeToken = loginAndGetToken("employee@example.com", "password123");
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("token").asText();
    }

    // Role Tests
    @Test
    void getRoles_WithAdminToken_ShouldReturnAllRoles() throws Exception {
        mockMvc.perform(get("/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void getRoles_WithFinanceToken_ShouldReturnAllRoles() throws Exception {
        mockMvc.perform(get("/roles")
                        .header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getRoles_WithEmployeeToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/roles")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRoles_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/roles"))
                .andExpect(status().isUnauthorized());
    }

    // Department Tests
    @Test
    void createDepartment_WithAdminToken_ShouldCreateDepartment() throws Exception {
        CreateDepartmentRequest request = new CreateDepartmentRequest("Engineering", "ENG", null);

        mockMvc.perform(post("/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Engineering"))
                .andExpect(jsonPath("$.code").value("ENG"));
    }

    @Test
    void createDepartment_WithFinanceToken_ShouldReturnForbidden() throws Exception {
        CreateDepartmentRequest request = new CreateDepartmentRequest("Engineering", "ENG", null);

        mockMvc.perform(post("/departments")
                        .header("Authorization", "Bearer " + financeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDepartment_WithEmployeeToken_ShouldReturnForbidden() throws Exception {
        CreateDepartmentRequest request = new CreateDepartmentRequest("Engineering", "ENG", null);

        mockMvc.perform(post("/departments")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllDepartments_WithAdminToken_ShouldReturnDepartments() throws Exception {
        // First create a department
        CreateDepartmentRequest request = new CreateDepartmentRequest("Engineering", "ENG", null);
        mockMvc.perform(post("/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then get all departments
        mockMvc.perform(get("/departments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllDepartments_WithFinanceToken_ShouldReturnDepartments() throws Exception {
        mockMvc.perform(get("/departments")
                        .header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllDepartments_WithEmployeeToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/departments")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateDepartment_WithAdminToken_ShouldUpdateDepartment() throws Exception {
        // First create a department
        CreateDepartmentRequest createRequest = new CreateDepartmentRequest("Engineering", "ENG", null);
        MvcResult createResult = mockMvc.perform(post("/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createdJson = createResult.getResponse().getContentAsString();
        String departmentId = objectMapper.readTree(createdJson).get("id").asText();

        // Then update the department
        UpdateDepartmentRequest updateRequest = new UpdateDepartmentRequest("Engineering Updated", null);
        mockMvc.perform(patch("/departments/{id}", departmentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Engineering Updated"));
    }

    @Test
    void updateDepartment_WithFinanceToken_ShouldReturnForbidden() throws Exception {
        UUID departmentId = UUID.randomUUID();
        UpdateDepartmentRequest request = new UpdateDepartmentRequest("Updated Name", null);

        mockMvc.perform(patch("/departments/{id}", departmentId)
                        .header("Authorization", "Bearer " + financeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDepartment_WithDuplicateCode_ShouldReturnBadRequest() throws Exception {
        // Create first department
        CreateDepartmentRequest request = new CreateDepartmentRequest("Engineering", "ENG", null);
        mockMvc.perform(post("/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Try to create another with same code - should return 400 since RuntimeException is handled by GlobalExceptionHandler
        mockMvc.perform(post("/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
