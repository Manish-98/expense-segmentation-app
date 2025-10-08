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
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserService userService;
    private final RoleService roleService;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            @Lazy UserService userService,
            RoleService roleService,
            DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.userService = userService;
        this.roleService = roleService;
        this.departmentMapper = departmentMapper;
    }

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Creating new department with code: {}", request.getCode());

        validateDepartmentCodeUnique(request.getCode());

        Department department = buildDepartment(request);
        Department saved = departmentRepository.save(department);

        if (department.getManager() != null) {
            assignUserAsManager(department.getManager(), saved);
        }

        log.info("Successfully created department: {} with id: {}", saved.getCode(), saved.getId());
        return departmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        log.debug("Fetching all departments with manager");
        List<DepartmentResponse> departments =
                departmentRepository.findAllWithManager().stream()
                        .map(departmentMapper::toResponse)
                        .toList();
        log.info("Retrieved {} departments", departments.size());
        return departments;
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(UUID id) {
        log.debug("Fetching department with id: {}", id);
        Department department = findDepartmentByIdWithManager(id);
        return departmentMapper.toResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(UUID id, UpdateDepartmentRequest request) {
        log.info("Updating department: {}", id);
        Department department = findDepartmentById(id);

        updateDepartmentName(department, request);
        updateDepartmentManager(department, request);

        Department updated = departmentRepository.save(department);
        log.info("Successfully updated department: {}", id);
        return departmentMapper.toResponse(updated);
    }

    /**
     * Gets a department entity by ID. Used internally by other services.
     *
     * @param departmentId the department ID
     * @return the department entity
     * @throws ResourceNotFoundException if department not found
     */
    @Transactional(readOnly = true)
    public Department getDepartmentEntityById(UUID departmentId) {
        return findDepartmentById(departmentId);
    }

    /**
     * Updates a department entity. Used internally by other services. Uses MANDATORY propagation to
     * ensure it runs within the caller's transaction.
     *
     * @param department the department to update
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void updateDepartmentEntity(Department department) {
        log.debug("Updating department entity: {}", department.getId());
        departmentRepository.save(department);
    }

    private void validateDepartmentCodeUnique(String code) {
        if (departmentRepository.existsByCode(code)) {
            log.error("Department creation failed: code {} already exists", code);
            throw new DuplicateResourceException("Department", "code", code);
        }
    }

    private Department buildDepartment(CreateDepartmentRequest request) {
        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());

        if (request.getManagerId() != null) {
            log.debug(
                    "Setting manager {} for department {}",
                    request.getManagerId(),
                    request.getCode());
            User manager = userService.getUserEntityById(request.getManagerId());
            department.setManager(manager);
        }

        return department;
    }

    private void updateDepartmentName(Department department, UpdateDepartmentRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            log.debug("Updating department {} name to: {}", department.getId(), request.getName());
            department.setName(request.getName());
        }
    }

    private void updateDepartmentManager(Department department, UpdateDepartmentRequest request) {
        if (request.getManagerId() == null) {
            return;
        }

        log.debug(
                "Updating department {} manager to: {}",
                department.getId(),
                request.getManagerId());

        User newManager = userService.getUserEntityById(request.getManagerId());
        User oldManager = department.getManager();

        if (oldManager != null && !oldManager.getId().equals(newManager.getId())) {
            log.info(
                    "Replacing department manager: old={}, new={}",
                    oldManager.getId(),
                    newManager.getId());
            // Note: Old manager's role and department are NOT automatically reverted.
            // Business rules:
            // 1. User may be managing multiple departments
            // 2. User may need MANAGER role for other responsibilities
            // 3. Manual intervention required if role demotion is needed
            // If automatic demotion is required in the future, implement it here
        }

        department.setManager(newManager);
        assignUserAsManager(newManager, department);
    }

    /**
     * Updates a user to be a manager of the given department. Sets the user's role to MANAGER and
     * department to the provided department. Explicitly saves the user to ensure changes are
     * persisted.
     *
     * @param user the user to update
     * @param department the department to assign
     */
    private void assignUserAsManager(User user, Department department) {
        log.debug(
                "Updating user {} to be manager of department {}",
                user.getId(),
                department.getId());

        Role managerRole = roleService.getRoleByName(RoleType.MANAGER);
        user.setRole(managerRole);
        user.setDepartment(department);
        userService.updateUserEntity(user);

        log.info(
                "Successfully updated user {} to MANAGER role in department {}",
                user.getId(),
                department.getCode());
    }

    private Department findDepartmentById(UUID id) {
        return departmentRepository
                .findById(id)
                .orElseThrow(
                        () -> {
                            log.error("Department not found with id: {}", id);
                            return new ResourceNotFoundException("Department", id.toString());
                        });
    }

    private Department findDepartmentByIdWithManager(UUID id) {
        return departmentRepository
                .findByIdWithManager(id)
                .orElseThrow(
                        () -> {
                            log.error("Department not found with id: {}", id);
                            return new ResourceNotFoundException("Department", id.toString());
                        });
    }
}
