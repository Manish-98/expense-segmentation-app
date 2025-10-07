package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.expense.segmentation.dto.CreateDepartmentRequest;
import com.expense.segmentation.dto.DepartmentResponse;
import com.expense.segmentation.dto.UpdateDepartmentRequest;
import com.expense.segmentation.model.Department;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import com.expense.segmentation.repository.DepartmentRepository;
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
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks private DepartmentService departmentService;

    private Department department;
    private User manager;
    private Role managerRole;

    @BeforeEach
    void setUp() {
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
        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));
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
    }

    @Test
    void createDepartment_WithDuplicateCode_ShouldThrowException() {
        // Given
        CreateDepartmentRequest request = new CreateDepartmentRequest("Engineering", "ENG", null);
        when(departmentRepository.existsByCode("ENG")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createDepartment_WithInvalidManagerId_ShouldThrowException() {
        // Given
        UUID invalidManagerId = UUID.randomUUID();
        CreateDepartmentRequest request =
                new CreateDepartmentRequest("Engineering", "ENG", invalidManagerId);
        when(departmentRepository.existsByCode("ENG")).thenReturn(false);
        when(userRepository.findById(invalidManagerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Manager not found");
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

        when(departmentRepository.findAll()).thenReturn(Arrays.asList(department, department2));

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
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));

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
        when(departmentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> departmentService.getDepartmentById(nonExistingId))
                .isInstanceOf(RuntimeException.class)
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
        when(userRepository.findById(newManager.getId())).thenReturn(Optional.of(newManager));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        // When
        DepartmentResponse response =
                departmentService.updateDepartment(department.getId(), request);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository).findById(newManager.getId());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void updateDepartment_WithNonExistingId_ShouldThrowException() {
        // Given
        UUID nonExistingId = UUID.randomUUID();
        UpdateDepartmentRequest request = new UpdateDepartmentRequest("Updated Name", null);
        when(departmentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> departmentService.updateDepartment(nonExistingId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Department not found");
    }

    @Test
    void updateDepartment_WithInvalidManagerId_ShouldThrowException() {
        // Given
        UUID invalidManagerId = UUID.randomUUID();
        UpdateDepartmentRequest request = new UpdateDepartmentRequest(null, invalidManagerId);
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
        when(userRepository.findById(invalidManagerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> departmentService.updateDepartment(department.getId(), request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Manager not found");
    }
}
