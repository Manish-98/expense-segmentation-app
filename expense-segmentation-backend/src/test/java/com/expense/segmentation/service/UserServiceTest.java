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
import com.expense.segmentation.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private RoleService roleService;

    @Mock private DepartmentService departmentService;

    private UserService userService;

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
        userService = new UserService(userRepository, roleService, departmentService, userMapper);

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
        when(userRepository.findAllWithDepartmentAndRole())
                .thenReturn(Arrays.asList(user1, user2, manager));

        // When
        List<UserResponse> users = userService.getAllUsers();

        // Then
        assertThat(users).hasSize(3);
        assertThat(users.get(0).getName()).isEqualTo("User One");
        assertThat(users.get(1).getName()).isEqualTo("User Two");
        assertThat(users.get(2).getName()).isEqualTo("Manager");
        verify(userRepository).findAllWithDepartmentAndRole();
    }

    @Test
    void getUsersByDepartment_WithValidManager_ShouldReturnUsersInDepartment() {
        // Given
        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(userRepository.findByDepartmentIdWithRole(department.getId()))
                .thenReturn(Arrays.asList(user1, user2, manager));

        // When
        List<UserResponse> users = userService.getUsersByDepartment(manager.getId());

        // Then
        assertThat(users).hasSize(3);
        assertThat(users)
                .extracting(UserResponse::getDepartmentId)
                .containsOnly(department.getId());
        verify(userRepository).findById(manager.getId());
        verify(userRepository).findByDepartmentIdWithRole(department.getId());
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
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(RoleType.MANAGER);
        verify(userRepository).findById(user1.getId());
        verify(roleService).getRoleByName(RoleType.MANAGER);
        verify(departmentService)
                .updateDepartmentEntity(department); // Should update department manager
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
        when(departmentService.getDepartmentEntityById(newDepartment.getId()))
                .thenReturn(newDepartment);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).findById(user1.getId());
        verify(departmentService).getDepartmentEntityById(newDepartment.getId());
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
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);
        when(departmentService.getDepartmentEntityById(newDepartment.getId()))
                .thenReturn(newDepartment);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).findById(user1.getId());
        verify(roleService).getRoleByName(RoleType.MANAGER);
        verify(departmentService).getDepartmentEntityById(newDepartment.getId());
        verify(departmentService)
                .updateDepartmentEntity(newDepartment); // Should update department manager
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
        verify(roleService, never()).getRoleByName(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenRoleNotFound_ShouldThrowException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.OWNER);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(roleService.getRoleByName(RoleType.OWNER))
                .thenThrow(new ResourceNotFoundException("Role", "name", RoleType.OWNER.toString()));

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(user1.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found");

        verify(userRepository).findById(user1.getId());
        verify(roleService).getRoleByName(RoleType.OWNER);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenDepartmentNotFound_ShouldThrowException() {
        // Given
        UUID nonExistentDeptId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setDepartmentId(nonExistentDeptId);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(departmentService.getDepartmentEntityById(nonExistentDeptId))
                .thenThrow(
                        new ResourceNotFoundException("Department", nonExistentDeptId.toString()));

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(user1.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found");

        verify(userRepository).findById(user1.getId());
        verify(departmentService).getDepartmentEntityById(nonExistentDeptId);
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
        verify(roleService, never()).getRoleByName(any());
        verify(departmentService, never()).getDepartmentEntityById(any());
        verify(userRepository).save(user1);
    }

    @Test
    void updateUser_PromoteToManager_ShouldUpdateDepartmentManagerField() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(RoleType.MANAGER);

        // Verify that department's manager field is updated
        verify(departmentService).updateDepartmentEntity(department);
        assertThat(department.getManager()).isEqualTo(user1);

        verify(userRepository).findById(user1.getId());
        verify(roleService).getRoleByName(RoleType.MANAGER);
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
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);
        when(departmentService.getDepartmentEntityById(newDepartment.getId()))
                .thenReturn(newDepartment);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        UserResponse response = userService.updateUser(user1.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(RoleType.MANAGER);

        // Verify that the new department's manager field is updated
        verify(departmentService).updateDepartmentEntity(newDepartment);
        assertThat(newDepartment.getManager()).isEqualTo(user1);

        verify(userRepository).findById(user1.getId());
        verify(roleService).getRoleByName(RoleType.MANAGER);
        verify(departmentService).getDepartmentEntityById(newDepartment.getId());
        verify(userRepository).save(user1);
    }

    @Test
    void updateUser_PromoteToManagerWithoutDepartment_ShouldThrowException() {
        // Given
        User userWithoutDept = new User();
        userWithoutDept.setId(UUID.randomUUID());
        userWithoutDept.setName("User Without Dept");
        userWithoutDept.setEmail("nodept@example.com");
        userWithoutDept.setRole(employeeRole);
        userWithoutDept.setDepartment(null);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);

        when(userRepository.findById(userWithoutDept.getId()))
                .thenReturn(Optional.of(userWithoutDept));
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userWithoutDept.getId(), request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining(
                        "User must be assigned to a department before being promoted to MANAGER"
                                + " role");

        verify(userRepository).findById(userWithoutDept.getId());
        verify(roleService).getRoleByName(RoleType.MANAGER);
        verify(departmentService, never()).updateDepartmentEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_PromoteToManagerWhenDepartmentHasManager_ShouldThrowException() {
        // Given
        User existingManager = new User();
        existingManager.setId(UUID.randomUUID());
        existingManager.setName("Existing Manager");
        existingManager.setEmail("existing@example.com");
        existingManager.setRole(managerRole);

        department.setManager(existingManager);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(user1.getId(), request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already has a manager")
                .hasMessageContaining("Existing Manager");

        verify(userRepository).findById(user1.getId());
        verify(roleService).getRoleByName(RoleType.MANAGER);
        verify(departmentService, never()).updateDepartmentEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_DemoteManagerToEmployee_ShouldClearDepartmentManager() {
        // Given
        department.setManager(manager);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.EMPLOYEE);

        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(roleService.getRoleByName(RoleType.EMPLOYEE)).thenReturn(employeeRole);
        when(userRepository.save(any(User.class))).thenReturn(manager);

        // When
        UserResponse response = userService.updateUser(manager.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(RoleType.EMPLOYEE);

        // Verify department's manager field is cleared
        verify(departmentService).updateDepartmentEntity(department);
        assertThat(department.getManager()).isNull();

        verify(userRepository).findById(manager.getId());
        verify(roleService).getRoleByName(RoleType.EMPLOYEE);
        verify(userRepository).save(manager);
    }

    @Test
    void updateUser_ManagerChangingDepartments_ShouldClearOldDepartmentManager() {
        // Given
        Department newDepartment = new Department();
        newDepartment.setId(UUID.randomUUID());
        newDepartment.setName("Sales");
        newDepartment.setCode("SALES");

        department.setManager(manager);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setDepartmentId(newDepartment.getId());

        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(departmentService.getDepartmentEntityById(newDepartment.getId()))
                .thenReturn(newDepartment);
        when(userRepository.save(any(User.class))).thenReturn(manager);

        // When
        UserResponse response = userService.updateUser(manager.getId(), request);

        // Then
        assertThat(response).isNotNull();

        // Verify old department's manager field is cleared
        verify(departmentService, times(2)).updateDepartmentEntity(any(Department.class));
        assertThat(department.getManager()).isNull();
        assertThat(newDepartment.getManager()).isEqualTo(manager);

        verify(userRepository).findById(manager.getId());
        verify(departmentService).getDepartmentEntityById(newDepartment.getId());
        verify(userRepository).save(manager);
    }

    @Test
    void updateUser_ManagerChangingToNewDepartmentWithExistingManager_ShouldThrowException() {
        // Given
        Department newDepartment = new Department();
        newDepartment.setId(UUID.randomUUID());
        newDepartment.setName("Sales");
        newDepartment.setCode("SALES");

        User existingManagerInNewDept = new User();
        existingManagerInNewDept.setId(UUID.randomUUID());
        existingManagerInNewDept.setName("Sales Manager");
        existingManagerInNewDept.setEmail("sales@example.com");
        existingManagerInNewDept.setRole(managerRole);

        newDepartment.setManager(existingManagerInNewDept);
        department.setManager(manager);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setDepartmentId(newDepartment.getId());

        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(departmentService.getDepartmentEntityById(newDepartment.getId()))
                .thenReturn(newDepartment);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(manager.getId(), request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already has a manager");

        // Verify old department's manager is cleared but user is not saved
        verify(departmentService).updateDepartmentEntity(department);
        assertThat(department.getManager()).isNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_DemoteManagerWithoutDepartment_ShouldNotThrowException() {
        // Given
        User managerWithoutDept = new User();
        managerWithoutDept.setId(UUID.randomUUID());
        managerWithoutDept.setName("Manager Without Dept");
        managerWithoutDept.setEmail("mgrnodept@example.com");
        managerWithoutDept.setRole(managerRole);
        managerWithoutDept.setDepartment(null);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.EMPLOYEE);

        when(userRepository.findById(managerWithoutDept.getId()))
                .thenReturn(Optional.of(managerWithoutDept));
        when(roleService.getRoleByName(RoleType.EMPLOYEE)).thenReturn(employeeRole);
        when(userRepository.save(any(User.class))).thenReturn(managerWithoutDept);

        // When
        UserResponse response = userService.updateUser(managerWithoutDept.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(RoleType.EMPLOYEE);

        verify(userRepository).findById(managerWithoutDept.getId());
        verify(roleService).getRoleByName(RoleType.EMPLOYEE);
        verify(departmentService, never()).updateDepartmentEntity(any());
        verify(userRepository).save(managerWithoutDept);
    }

    @Test
    void updateUser_PromoteToManagerInSameDepartmentAsExistingManager_ShouldThrowException() {
        // Given
        department.setManager(manager);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(RoleType.MANAGER);
        // user1 is already in the same department

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(user1.getId(), request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already has a manager");

        verify(userRepository).findById(user1.getId());
        verify(roleService).getRoleByName(RoleType.MANAGER);
        verify(departmentService, never()).updateDepartmentEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivateUser_WhenUserIsManager_ShouldClearDepartmentManager() {
        // Given
        department.setManager(manager);

        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
        when(userRepository.save(any(User.class))).thenReturn(manager);

        // When
        userService.deactivateUser(manager.getId());

        // Then
        assertThat(manager.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(department.getManager()).isNull();

        verify(userRepository).findById(manager.getId());
        verify(departmentService).updateDepartmentEntity(department);
        verify(userRepository).save(manager);
    }

    @Test
    void deactivateUser_WhenUserIsNotManager_ShouldNotUpdateDepartment() {
        // Given
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        userService.deactivateUser(user1.getId());

        // Then
        assertThat(user1.getStatus()).isEqualTo(UserStatus.INACTIVE);

        verify(userRepository).findById(user1.getId());
        verify(departmentService, never()).updateDepartmentEntity(any());
        verify(userRepository).save(user1);
    }
}
