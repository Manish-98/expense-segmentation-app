package com.expense.segmentation.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expense.segmentation.dto.UpdateUserRequest;
import com.expense.segmentation.model.Department;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.repository.DepartmentRepository;
import com.expense.segmentation.repository.RoleRepository;
import com.expense.segmentation.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserManagementIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private DepartmentRepository departmentRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String managerToken;
    private String employeeToken;
    private User adminUser;
    private User managerUser;
    private User employeeUser;
    private Department department1;
    private Department department2;

    @BeforeEach
    void setUp() throws Exception {
        // Create departments
        department1 = new Department();
        department1.setName("Engineering");
        department1.setCode("ENG");
        department1 = departmentRepository.save(department1);

        department2 = new Department();
        department2.setName("Sales");
        department2.setCode("SALES");
        department2 = departmentRepository.save(department2);

        // Create admin user and get token
        Role adminRole =
                roleRepository
                        .findByName(RoleType.ADMIN)
                        .orElseThrow(() -> new RuntimeException("Admin role not found"));

        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPasswordHash(passwordEncoder.encode("password123"));
        adminUser.setRole(adminRole);
        adminUser = userRepository.save(adminUser);

        adminToken = loginAndGetToken("admin@test.com", "password123");

        // Create manager user and get token
        Role managerRole =
                roleRepository
                        .findByName(RoleType.MANAGER)
                        .orElseThrow(() -> new RuntimeException("Manager role not found"));

        managerUser = new User();
        managerUser.setName("Manager User");
        managerUser.setEmail("manager@test.com");
        managerUser.setPasswordHash(passwordEncoder.encode("password123"));
        managerUser.setRole(managerRole);
        managerUser.setDepartment(department1);
        managerUser = userRepository.save(managerUser);

        managerToken = loginAndGetToken("manager@test.com", "password123");

        // Create employee user and get token
        Role employeeRole =
                roleRepository
                        .findByName(RoleType.EMPLOYEE)
                        .orElseThrow(() -> new RuntimeException("Employee role not found"));

        employeeUser = new User();
        employeeUser.setName("Employee User");
        employeeUser.setEmail("employee@test.com");
        employeeUser.setPasswordHash(passwordEncoder.encode("password123"));
        employeeUser.setRole(employeeRole);
        employeeUser.setDepartment(department1);
        employeeUser = userRepository.save(employeeUser);

        employeeToken = loginAndGetToken("employee@test.com", "password123");

        // Create additional employee in same department as manager
        User employee2 = new User();
        employee2.setName("Employee Two");
        employee2.setEmail("employee2@test.com");
        employee2.setPasswordHash(passwordEncoder.encode("password123"));
        employee2.setRole(employeeRole);
        employee2.setDepartment(department1);
        userRepository.save(employee2);

        // Create employee in different department
        User employee3 = new User();
        employee3.setName("Employee Three");
        employee3.setEmail("employee3@test.com");
        employee3.setPasswordHash(passwordEncoder.encode("password123"));
        employee3.setRole(employeeRole);
        employee3.setDepartment(department2);
        userRepository.save(employee3);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        MvcResult result =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginJson))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("token").asText();
    }

    // GET /users tests
    @Test
    void getAllUsers_WithAdminToken_ShouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5)); // admin, manager, 3 employees
    }

    @Test
    void getAllUsers_WithManagerToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/users").header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_WithEmployeeToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/users").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
    }

    // GET /users/department tests
    @Test
    void getUsersInDepartment_WithManagerToken_ShouldReturnOnlyDepartmentUsers() throws Exception {
        mockMvc.perform(get("/users/department").header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3)) // manager + 2 employees in department1
                .andExpect(jsonPath("$[0].departmentId").value(department1.getId().toString()))
                .andExpect(jsonPath("$[1].departmentId").value(department1.getId().toString()))
                .andExpect(jsonPath("$[2].departmentId").value(department1.getId().toString()));
    }

    @Test
    void getUsersInDepartment_WithAdminToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/users/department").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsersInDepartment_WithEmployeeToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/users/department").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsersInDepartment_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/users/department")).andExpect(status().isUnauthorized());
    }

    // PATCH /users/{id} tests
    @Test
    void updateUser_WithAdminToken_ShouldUpdateUserRole() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);

        mockMvc.perform(
                        patch("/users/{id}", employeeUser.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.id").value(employeeUser.getId().toString()));
    }

    @Test
    void updateUser_WithAdminToken_ShouldUpdateUserDepartment() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setDepartmentId(department2.getId());

        mockMvc.perform(
                        patch("/users/{id}", employeeUser.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentId").value(department2.getId().toString()))
                .andExpect(jsonPath("$.departmentName").value("Sales"));
    }

    @Test
    void updateUser_WithManagerToken_ShouldReturnForbidden() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);

        mockMvc.perform(
                        patch("/users/{id}", employeeUser.getId())
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_WithEmployeeToken_ShouldReturnForbidden() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);

        mockMvc.perform(
                        patch("/users/{id}", managerUser.getId())
                                .header("Authorization", "Bearer " + employeeToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_WithNonExistentUserId_ShouldReturnBadRequest() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);

        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(
                        patch("/users/{id}", nonExistentId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // DELETE /users/{id} tests
    @Test
    void deactivateUser_WithAdminToken_ShouldDeactivateUser() throws Exception {
        mockMvc.perform(
                        delete("/users/{id}", employeeUser.getId())
                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify user is deactivated
        mockMvc.perform(get("/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[?(@.id == '" + employeeUser.getId() + "')].status")
                                .value("INACTIVE"));
    }

    @Test
    void deactivateUser_WithManagerToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(
                        delete("/users/{id}", employeeUser.getId())
                                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deactivateUser_WithEmployeeToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(
                        delete("/users/{id}", managerUser.getId())
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deactivateUser_WithNonExistentUserId_ShouldReturnBadRequest() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(
                        delete("/users/{id}", nonExistentId)
                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }
}
