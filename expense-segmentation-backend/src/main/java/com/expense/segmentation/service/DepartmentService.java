package com.expense.segmentation.service;

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
import com.expense.segmentation.repository.DepartmentRepository;
import com.expense.segmentation.repository.RoleRepository;
import com.expense.segmentation.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Creating new department with code: {}", request.getCode());

        // Check if department code already exists
        if (departmentRepository.existsByCode(request.getCode())) {
            log.error("Department creation failed: code {} already exists", request.getCode());
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());

        // Set manager if provided
        User manager = null;
        if (request.getManagerId() != null) {
            log.debug(
                    "Setting manager {} for department {}",
                    request.getManagerId(),
                    request.getCode());
            manager =
                    userRepository
                            .findById(request.getManagerId())
                            .orElseThrow(
                                    () -> {
                                        log.error(
                                                "Manager not found with id: {}",
                                                request.getManagerId());
                                        return new ResourceNotFoundException(
                                                "User", request.getManagerId().toString());
                                    });
            department.setManager(manager);
        }

        Department saved = departmentRepository.save(department);

        // Update manager's role to MANAGER and department after saving
        if (manager != null) {
            updateUserToManager(manager, saved);
        }

        log.info("Successfully created department: {} with id: {}", saved.getCode(), saved.getId());
        return departmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        log.debug("Fetching all departments");
        List<DepartmentResponse> departments =
                departmentRepository.findAll().stream()
                        .map(departmentMapper::toResponse)
                        .collect(Collectors.toList());
        log.info("Retrieved {} departments", departments.size());
        return departments;
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(UUID id) {
        log.debug("Fetching department with id: {}", id);
        Department department =
                departmentRepository
                        .findById(id)
                        .orElseThrow(
                                () -> {
                                    log.error("Department not found with id: {}", id);
                                    return new ResourceNotFoundException(
                                            "Department", id.toString());
                                });
        return departmentMapper.toResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(UUID id, UpdateDepartmentRequest request) {
        log.info("Updating department: {}", id);
        Department department =
                departmentRepository
                        .findById(id)
                        .orElseThrow(
                                () -> {
                                    log.error("Department not found with id: {}", id);
                                    return new ResourceNotFoundException(
                                            "Department", id.toString());
                                });

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            log.debug("Updating department {} name to: {}", id, request.getName());
            department.setName(request.getName());
        }

        // Update manager if provided
        if (request.getManagerId() != null) {
            log.debug("Updating department {} manager to: {}", id, request.getManagerId());

            // Get the new manager
            User newManager =
                    userRepository
                            .findById(request.getManagerId())
                            .orElseThrow(
                                    () -> {
                                        log.error(
                                                "Manager not found with id: {}",
                                                request.getManagerId());
                                        return new ResourceNotFoundException(
                                                "User", request.getManagerId().toString());
                                    });

            // Handle old manager if exists
            User oldManager = department.getManager();
            if (oldManager != null && !oldManager.getId().equals(newManager.getId())) {
                log.debug("Removing manager role from previous manager: {}", oldManager.getId());
                // Note: Old manager's role/department are not automatically reverted
                // This is intentional as they may still need MANAGER role for other reasons
                // Business logic can be adjusted based on requirements
            }

            department.setManager(newManager);

            // Update new manager's role to MANAGER and department
            updateUserToManager(newManager, department);
        }

        Department updated = departmentRepository.save(department);
        log.info("Successfully updated department: {}", id);
        return departmentMapper.toResponse(updated);
    }

    /**
     * Updates a user to be a manager of the given department.
     * Sets the user's role to MANAGER and department to the provided department.
     * Changes are persisted via JPA change tracking within the transaction.
     *
     * @param user the user to update
     * @param department the department to assign
     */
    private void updateUserToManager(User user, Department department) {
        log.debug("Updating user {} to be manager of department {}", user.getId(), department.getId());

        // Get MANAGER role
        Role managerRole = roleRepository.findByName(RoleType.MANAGER)
                .orElseThrow(() -> {
                    log.error("MANAGER role not found in database");
                    return new ResourceNotFoundException("Role", "MANAGER");
                });

        // Update user's role and department
        // JPA will automatically persist these changes at transaction commit
        user.setRole(managerRole);
        user.setDepartment(department);

        log.info("Successfully updated user {} to MANAGER role in department {}",
                user.getId(), department.getCode());
    }
}
