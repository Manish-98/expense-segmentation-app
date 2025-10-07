package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.expense.segmentation.dto.RoleResponse;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.repository.RoleRepository;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock private RoleRepository roleRepository;

    @InjectMocks private RoleService roleService;

    private Role employeeRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        employeeRole = new Role();
        employeeRole.setId(UUID.randomUUID());
        employeeRole.setName(RoleType.EMPLOYEE);
        employeeRole.setDescription("Employee role");

        adminRole = new Role();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName(RoleType.ADMIN);
        adminRole.setDescription("Admin role");
    }

    @Test
    void getAllRoles_ShouldReturnAllRoles() {
        // Given
        when(roleRepository.findAll()).thenReturn(Arrays.asList(employeeRole, adminRole));

        // When
        List<RoleResponse> roles = roleService.getAllRoles();

        // Then
        assertThat(roles).hasSize(2);
        assertThat(roles.get(0).getName()).isEqualTo(RoleType.EMPLOYEE);
        assertThat(roles.get(0).getDescription()).isEqualTo("Employee role");
        assertThat(roles.get(1).getName()).isEqualTo(RoleType.ADMIN);
        assertThat(roles.get(1).getDescription()).isEqualTo("Admin role");
    }

    @Test
    void getAllRoles_WhenNoRoles_ShouldReturnEmptyList() {
        // Given
        when(roleRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<RoleResponse> roles = roleService.getAllRoles();

        // Then
        assertThat(roles).isEmpty();
    }
}
