package com.expense.segmentation.integration;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.expense.segmentation.config.JwtAuthenticationFilter;
import com.expense.segmentation.config.JwtTokenUtil;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import com.expense.segmentation.repository.RoleRepository;
import com.expense.segmentation.repository.UserRepository;
import com.expense.segmentation.service.CustomUserDetailsService;
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
class RoleIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private RoleRepository roleRepository;

    @Autowired private UserRepository userRepository;

    @MockBean private JwtTokenUtil jwtTokenUtil;

    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean private CustomUserDetailsService customUserDetailsService;

    private Role testRole;
    private User adminUser;
    private User financeUser;

    @BeforeEach
    void setUp() {
        // Clean up data to avoid conflicts
        roleRepository.deleteAll();
        userRepository.deleteAll();

        // Create test role
        testRole = new Role();
        testRole.setName(RoleType.EMPLOYEE);
        testRole.setDescription("Employee role");
        testRole = roleRepository.save(testRole);

        // Create admin user
        Role adminRole = new Role();
        adminRole.setName(RoleType.ADMIN);
        adminRole.setDescription("Admin role");
        adminRole = roleRepository.save(adminRole);

        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("password");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setRole(adminRole);
        adminUser = userRepository.save(adminUser);

        // Create finance user
        Role financeRole = new Role();
        financeRole.setName(RoleType.FINANCE);
        financeRole.setDescription("Finance role");
        financeRole = roleRepository.save(financeRole);

        financeUser = new User();
        financeUser.setName("Finance User");
        financeUser.setEmail("finance@example.com");
        financeUser.setPasswordHash("password");
        financeUser.setStatus(UserStatus.ACTIVE);
        financeUser.setRole(financeRole);
        financeUser = userRepository.save(financeUser);

        // Mock custom user details service to return our test users
        when(customUserDetailsService.loadUserByUsername("admin@example.com"))
                .thenReturn(
                        org.springframework.security.core.userdetails.User.builder()
                                .username(adminUser.getEmail())
                                .password("password")
                                .authorities("ROLE_ADMIN")
                                .build());

        when(customUserDetailsService.loadUserByUsername("finance@example.com"))
                .thenReturn(
                        org.springframework.security.core.userdetails.User.builder()
                                .username(financeUser.getEmail())
                                .password("password")
                                .authorities("ROLE_FINANCE")
                                .build());

        // Create employee user for employee role tests
        User employeeUser = new User();
        employeeUser.setName("Employee User");
        employeeUser.setEmail("employee@example.com");
        employeeUser.setPasswordHash("password");
        employeeUser.setStatus(UserStatus.ACTIVE);
        employeeUser.setRole(testRole);
        userRepository.save(employeeUser);

        when(customUserDetailsService.loadUserByUsername("employee@example.com"))
                .thenReturn(
                        org.springframework.security.core.userdetails.User.builder()
                                .username("employee@example.com")
                                .password("password")
                                .authorities("ROLE_EMPLOYEE")
                                .build());
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = {"ADMIN"})
    void getAllRoles_WithAdminRole_ShouldReturnAllRoles() throws Exception {
        // When & Then
        mockMvc.perform(get("/roles").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(
                        jsonPath("$[?(@.name == 'EMPLOYEE')].description").value("Employee role"))
                .andExpect(jsonPath("$[?(@.name == 'ADMIN')].description").value("Admin role"))
                .andExpect(jsonPath("$[?(@.name == 'FINANCE')].description").value("Finance role"));
    }

    @Test
    @WithMockUser(
            username = "finance@example.com",
            roles = {"FINANCE"})
    void getAllRoles_WithFinanceRole_ShouldReturnAllRoles() throws Exception {
        // When & Then
        mockMvc.perform(get("/roles").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(
            username = "employee@example.com",
            roles = {"EMPLOYEE"})
    void getAllRoles_WithEmployeeRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/roles").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(
            username = "manager@example.com",
            roles = {"MANAGER"})
    void getAllRoles_WithManagerRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/roles").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
