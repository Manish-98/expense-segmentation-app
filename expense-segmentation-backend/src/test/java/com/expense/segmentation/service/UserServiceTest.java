package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.expense.segmentation.dto.UpdateUserRequest;
import com.expense.segmentation.dto.UserResponse;
import com.expense.segmentation.exception.InvalidOperationException;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.UserMapper;
import com.expense.segmentation.model.Department;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import com.expense.segmentation.repository.DepartmentRepository;
import com.expense.segmentation.repository.RoleRepository;
import com.expense.segmentation.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private RoleRepository roleRepository;

    @Mock private DepartmentRepository departmentRepository;

    @InjectMocks private UserService userService;

    private UserMapper userMapper;

    private User user1;
    private User user2;
    private User manager;
    private Role employeeRole;
    private Role managerRole;
    private Department department;

    @BeforeEach
    void setUp() {
        // Initialize real mapper
        userMapper = new UserMapper();
        userService =
                new UserService(userRepository, roleRepository, departmentRepository, userMapper);

        employeeRole = new Role();
        employeeRole.setId(UUID.randomUUID());
        employeeRole.setName(RoleType.EMPLOYEE);
        employeeRole.setDescription("Employee role");

        managerRole = new Role();
        managerRole.setId(UUID.randomUUID());
        managerRole.setName(RoleType.MANAGER);
        managerRole.setDescription("Manager role");

        department = new Department();
        department.setId(UUID.randomUUID());
        department.setName("Engineering");
        department.setCode("ENG");

        user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setName("User One");
        user1.setEmail("user1@example.com");
        user1.setPasswordHash("hashedPassword");
        user1.setRole(employeeRole);
        user1.setDepartment(department);
        user1.setStatus(UserStatus.ACTIVE);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());

        user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hashedPassword");
        user2.setRole(employeeRole);
        user2.setDepartment(department);
        user2.setStatus(UserStatus.ACTIVE);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());

        manager = new User();
        manager.setId(UUID.randomUUID());
        manager.setName("Manager");
        manager.setEmail("manager@example.com");
        manager.setPasswordHash("hashedPassword");
        manager.setRole(managerRole);
        manager.setDepartment(department);
        manager.setStatus(UserStatus.ACTIVE);
        manager.setCreatedAt(LocalDateTime.now());
        manager.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, manager));

        // When
        List<UserResponse> users = userService.getAllUsers();

        // Then
        assertThat(users).hasSize(3);
        assertThat(users.get(0).getName()).isEqualTo("User One");
        assertThat(users.get(1).getName()).isEqualTo("User Two");
        assertThat(users.get(2).getName()).isEqualTo("Manager");
        verify(userRepository).findAll();
    }

    @Test
    void getUsersByDepartment_WithValidManager_ShouldReturnUsersInDepartment() {
        // Given
        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(userRepository.findByDepartmentId(department.getId()))
                .thenReturn(Arrays.asList(user1, user2, manager));

        // When
        List<UserResponse> users = userService.getUsersByDepartment(manager.getId());

        // Then
        assertThat(users).hasSize(3);
        assertThat(users)
                .extracting(UserResponse::getDepartmentId)
                .containsOnly(department.getId());
        verify(userRepository).findById(manager.getId());
        verify(userRepository).findByDepartmentId(department.getId());
    }

    @Test
    void getUsersByDepartment_WhenManagerNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUsersByDepartment(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).findByDepartmentId(any());
    }

    @Test
    void getUsersByDepartment_WhenManagerHasNoDepartment_ShouldThrowException() {
        // Given
        manager.setDepartment(null);
        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));

        // When & Then
        assertThatThrownBy(() -> userService.getUsersByDepartment(manager.getId()))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Manager is not assigned to any department");

        verify(userRepository).findById(manager.getId());
        verify(userRepository, never()).findByDepartmentId(any());
    }

    @Test
    void updateUser_WithNewRole_ShouldUpdateRole() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(roleRepository.findByName(RoleType.MANAGER)).thenReturn(Optional.of(managerRole));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(RoleType.MANAGER);
        verify(userRepository).findById(user1.getId());
        verify(roleRepository).findByName(RoleType.MANAGER);
        verify(departmentRepository).save(department); // Should update department manager
        verify(userRepository).save(user1);
    }

    @Test
    void updateUser_WithNewDepartment_ShouldUpdateDepartment() {
        // Given
        Department newDepartment = new Department();
        newDepartment.setId(UUID.randomUUID());
        newDepartment.setName("Sales");
        newDepartment.setCode("SALES");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setDepartmentId(newDepartment.getId());

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(departmentRepository.findById(newDepartment.getId()))
                .thenReturn(Optional.of(newDepartment));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).findById(user1.getId());
        verify(departmentRepository).findById(newDepartment.getId());
        verify(userRepository).save(user1);
    }

    @Test
    void updateUser_WithBothRoleAndDepartment_ShouldUpdateBoth() {
        // Given
        Department newDepartment = new Department();
        newDepartment.setId(UUID.randomUUID());
        newDepartment.setName("Sales");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);
        request.setDepartmentId(newDepartment.getId());

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(roleRepository.findByName(RoleType.MANAGER)).thenReturn(Optional.of(managerRole));
        when(departmentRepository.findById(newDepartment.getId()))
                .thenReturn(Optional.of(newDepartment));
        when(departmentRepository.save(any(Department.class))).thenReturn(newDepartment);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).findById(user1.getId());
        verify(roleRepository).findByName(RoleType.MANAGER);
        verify(departmentRepository).findById(newDepartment.getId());
        verify(departmentRepository).save(newDepartment); // Should update department manager
        verify(userRepository).save(user1);
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);

        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(nonExistentId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(nonExistentId);
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenRoleNotFound_ShouldThrowException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.OWNER);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(roleRepository.findByName(RoleType.OWNER)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(user1.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found");

        verify(userRepository).findById(user1.getId());
        verify(roleRepository).findByName(RoleType.OWNER);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenDepartmentNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentDeptId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setDepartmentId(nonExistentDeptId);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(departmentRepository.findById(nonExistentDeptId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(user1.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found");

        verify(userRepository).findById(user1.getId());
        verify(departmentRepository).findById(nonExistentDeptId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivateUser_WithValidUserId_ShouldSetStatusToInactive() {
        // Given
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        userService.deactivateUser(user1.getId());

        // Then
        assertThat(user1.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).findById(user1.getId());
        verify(userRepository).save(user1);
    }

    @Test
    void deactivateUser_WhenUserNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deactivateUser(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WithEmptyRequest_ShouldNotUpdateAnything() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).findById(user1.getId());
        verify(roleRepository, never()).findByName(any());
        verify(departmentRepository, never()).findById(any());
        verify(userRepository).save(user1);
    }

    @Test
    void updateUser_PromoteToManager_ShouldUpdateDepartmentManagerField() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(roleRepository.findByName(RoleType.MANAGER)).thenReturn(Optional.of(managerRole));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(RoleType.MANAGER);

        // Verify that department's manager field is updated
        verify(departmentRepository).save(department);
        assertThat(department.getManager()).isEqualTo(user1);

        verify(userRepository).findById(user1.getId());
        verify(roleRepository).findByName(RoleType.MANAGER);
        verify(userRepository).save(user1);
    }

    @Test
    void updateUser_PromoteToManagerWithDepartmentChange_ShouldUpdateNewDepartmentManager() {
        // Given
        Department newDepartment = new Department();
        newDepartment.setId(UUID.randomUUID());
        newDepartment.setName("Sales");
        newDepartment.setCode("SALES");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);
        request.setDepartmentId(newDepartment.getId());

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(roleRepository.findByName(RoleType.MANAGER)).thenReturn(Optional.of(managerRole));
        when(departmentRepository.findById(newDepartment.getId()))
                .thenReturn(Optional.of(newDepartment));
        when(departmentRepository.save(any(Department.class))).thenReturn(newDepartment);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(RoleType.MANAGER);

        // Verify that the new department's manager field is updated
        verify(departmentRepository).save(newDepartment);
        assertThat(newDepartment.getManager()).isEqualTo(user1);

        verify(userRepository).findById(user1.getId());
        verify(roleRepository).findByName(RoleType.MANAGER);
        verify(departmentRepository).findById(newDepartment.getId());
        verify(userRepository).save(user1);
    }
}
