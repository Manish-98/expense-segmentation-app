package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.expense.segmentation.dto.CreateDepartmentRequest;
import com.expense.segmentation.dto.DepartmentResponse;
import com.expense.segmentation.dto.UpdateDepartmentRequest;
import com.expense.segmentation.exception.DuplicateResourceException;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.DepartmentMapper;
import com.expense.segmentation.model.Department;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import com.expense.segmentation.repository.DepartmentRepository;
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
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;

    @Mock private UserService userService;

    @Mock private RoleService roleService;

    private DepartmentService departmentService;

    private DepartmentMapper departmentMapper;

    private Department department;
    private User manager;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        // Initialize real mapper
        departmentMapper = new DepartmentMapper();
        departmentService =
                new DepartmentService(
                        departmentRepository, userService, roleService, departmentMapper);

        managerRole = new Role();
        managerRole.setId(UUID.randomUUID());
        managerRole.setName(RoleType.MANAGER);

        manager = new User();
        manager.setId(UUID.randomUUID());
        manager.setName("Manager User");
        manager.setEmail("manager@example.com");
        manager.setPasswordHash("hashedPassword");
        manager.setRole(managerRole);
        manager.setStatus(UserStatus.ACTIVE);

        department = new Department();
        department.setId(UUID.randomUUID());
        department.setName("Engineering");
        department.setCode("ENG");
        department.setManager(manager);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createDepartment_WithValidRequest_ShouldCreateDepartment() {
        // Given
        CreateDepartmentRequest request =
                new CreateDepartmentRequest("Engineering", "ENG", manager.getId());
        when(departmentRepository.existsByCode("ENG")).thenReturn(false);
        when(userService.getUserEntityById(manager.getId())).thenReturn(manager);
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentResponse response = departmentService.createDepartment(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Engineering");
        assertThat(response.getCode()).isEqualTo("ENG");
        assertThat(response.getManagerId()).isEqualTo(manager.getId());
        assertThat(response.getManagerName()).isEqualTo("Manager User");
        verify(departmentRepository).save(any(Department.class));
        verify(userService).updateUserEntity(manager); // Verify explicit user save
        // Verify user's role and department are updated
        assertThat(manager.getRole()).isEqualTo(managerRole);
        assertThat(manager.getDepartment()).isEqualTo(department);
    }

    @Test
    void createDepartment_WithDuplicateCode_ShouldThrowException() {
        // Given
        CreateDepartmentRequest request = new CreateDepartmentRequest("Engineering", "ENG", null);
        when(departmentRepository.existsByCode("ENG")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createDepartment_WithInvalidManagerId_ShouldThrowException() {
        // Given
        UUID invalidManagerId = UUID.randomUUID();
        CreateDepartmentRequest request =
                new CreateDepartmentRequest("Engineering", "ENG", invalidManagerId);
        when(departmentRepository.existsByCode("ENG")).thenReturn(false);
        when(userService.getUserEntityById(invalidManagerId)).thenThrow(new ResourceNotFoundException("User", invalidManagerId.toString()));

        // When & Then
        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getAllDepartments_ShouldReturnAllDepartments() {
        // Given
        Department department2 = new Department();
        department2.setId(UUID.randomUUID());
        department2.setName("Sales");
        department2.setCode("SALES");
        department2.setCreatedAt(LocalDateTime.now());
        department2.setUpdatedAt(LocalDateTime.now());

        when(departmentRepository.findAllWithManager())
                .thenReturn(Arrays.asList(department, department2));

        // When
        List<DepartmentResponse> departments = departmentService.getAllDepartments();

        // Then
        assertThat(departments).hasSize(2);
        assertThat(departments.get(0).getName()).isEqualTo("Engineering");
        assertThat(departments.get(1).getName()).isEqualTo("Sales");
    }

    @Test
    void getDepartmentById_WithExistingId_ShouldReturnDepartment() {
        // Given
        when(departmentRepository.findByIdWithManager(department.getId()))
                .thenReturn(Optional.of(department));

        // When
        DepartmentResponse response = departmentService.getDepartmentById(department.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(department.getId());
        assertThat(response.getName()).isEqualTo("Engineering");
    }

    @Test
    void getDepartmentById_WithNonExistingId_ShouldThrowException() {
        // Given
        UUID nonExistingId = UUID.randomUUID();
        when(departmentRepository.findByIdWithManager(nonExistingId)).thenThrow(new ResourceNotFoundException("Department", nonExistingId.toString()));

        // When & Then
        assertThatThrownBy(() -> departmentService.getDepartmentById(nonExistingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found");
    }

    @Test
    void updateDepartment_WithNewName_ShouldUpdateDepartment() {
        // Given
        UpdateDepartmentRequest request = new UpdateDepartmentRequest("Engineering Updated", null);
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentResponse response =
                departmentService.updateDepartment(department.getId(), request);

        // Then
        assertThat(response).isNotNull();
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void updateDepartment_WithNewManager_ShouldUpdateManager() {
        // Given
        User newManager = new User();
        newManager.setId(UUID.randomUUID());
        newManager.setName("New Manager");

        UpdateDepartmentRequest request = new UpdateDepartmentRequest(null, newManager.getId());
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(userService.getUserEntityById(newManager.getId())).thenReturn(newManager);
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentResponse response =
                departmentService.updateDepartment(department.getId(), request);

        // Then
        assertThat(response).isNotNull();
        verify(userService).getUserEntityById(newManager.getId());
        verify(departmentRepository).save(any(Department.class));
        verify(userService).updateUserEntity(newManager); // Verify explicit user save
        // Verify user's role and department are updated
        assertThat(newManager.getRole()).isEqualTo(managerRole);
        assertThat(newManager.getDepartment()).isEqualTo(department);
    }

    @Test
    void updateDepartment_WithNonExistingId_ShouldThrowException() {
        // Given
        UUID nonExistingId = UUID.randomUUID();
        UpdateDepartmentRequest request = new UpdateDepartmentRequest("Updated Name", null);
        when(departmentRepository.findById(nonExistingId)).thenThrow(new ResourceNotFoundException("Department", nonExistingId.toString()));

        // When & Then
        assertThatThrownBy(() -> departmentService.updateDepartment(nonExistingId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found");
    }

    @Test
    void updateDepartment_WithInvalidManagerId_ShouldThrowException() {
        // Given
        UUID invalidManagerId = UUID.randomUUID();
        UpdateDepartmentRequest request = new UpdateDepartmentRequest(null, invalidManagerId);
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(userService.getUserEntityById(invalidManagerId)).thenThrow(new ResourceNotFoundException("User", invalidManagerId.toString()));

        // When & Then
        assertThatThrownBy(() -> departmentService.updateDepartment(department.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateDepartment_WithBlankName_ShouldNotUpdateName() {
        // Given
        String originalName = department.getName();
        UpdateDepartmentRequest request = new UpdateDepartmentRequest("   ", null);
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentResponse response =
                departmentService.updateDepartment(department.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(department.getName()).isEqualTo(originalName); // Name should remain unchanged
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void createDepartment_WithoutManager_ShouldCreateDepartmentWithoutManager() {
        // Given
        CreateDepartmentRequest request = new CreateDepartmentRequest("Engineering", "ENG", null);
        Department departmentWithoutManager = new Department();
        departmentWithoutManager.setId(UUID.randomUUID());
        departmentWithoutManager.setName("Engineering");
        departmentWithoutManager.setCode("ENG");
        departmentWithoutManager.setCreatedAt(LocalDateTime.now());
        departmentWithoutManager.setUpdatedAt(LocalDateTime.now());

        when(departmentRepository.existsByCode("ENG")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(departmentWithoutManager);

        // When
        DepartmentResponse response = departmentService.createDepartment(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Engineering");
        assertThat(response.getCode()).isEqualTo("ENG");
        assertThat(response.getManagerId()).isNull();
        assertThat(response.getManagerName()).isNull();
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void updateDepartment_WhenReplacingManager_ShouldLogChange() {
        // Given - Department already has a manager
        User oldManager = new User();
        oldManager.setId(UUID.randomUUID());
        oldManager.setName("Old Manager");
        oldManager.setRole(managerRole);

        Department existingDepartment = new Department();
        existingDepartment.setId(UUID.randomUUID());
        existingDepartment.setName("Sales");
        existingDepartment.setCode("SALES");
        existingDepartment.setManager(oldManager);

        User newManager = new User();
        newManager.setId(UUID.randomUUID());
        newManager.setName("New Manager");

        UpdateDepartmentRequest request = new UpdateDepartmentRequest(null, newManager.getId());
        when(departmentRepository.findById(existingDepartment.getId()))
                .thenReturn(Optional.of(existingDepartment));
        when(userService.getUserEntityById(newManager.getId())).thenReturn(newManager);
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);
        when(departmentRepository.save(any(Department.class))).thenReturn(existingDepartment);

        // When
        DepartmentResponse response =
                departmentService.updateDepartment(existingDepartment.getId(), request);

        // Then
        assertThat(response).isNotNull();
        assertThat(existingDepartment.getManager()).isEqualTo(newManager);
        // Old manager's role remains unchanged (business rule)
        assertThat(oldManager.getRole()).isEqualTo(managerRole);
        verify(userService).updateUserEntity(newManager);
    }

    @Test
    void createDepartment_WithUserAlreadyManager_ShouldStillAssign() {
        // Given - User is already a manager with MANAGER role
        manager.setRole(managerRole);

        CreateDepartmentRequest request =
                new CreateDepartmentRequest("Engineering", "ENG", manager.getId());
        when(departmentRepository.existsByCode("ENG")).thenReturn(false);
        when(userService.getUserEntityById(manager.getId())).thenReturn(manager);
        when(roleService.getRoleByName(RoleType.MANAGER)).thenReturn(managerRole);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentResponse response = departmentService.createDepartment(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getManagerId()).isEqualTo(manager.getId());
        // User should still be saved with MANAGER role and department
        verify(userService).updateUserEntity(manager);
        assertThat(manager.getRole()).isEqualTo(managerRole);
        assertThat(manager.getDepartment()).isEqualTo(department);
    }
}
