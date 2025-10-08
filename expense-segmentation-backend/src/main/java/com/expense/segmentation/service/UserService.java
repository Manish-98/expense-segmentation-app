package com.expense.segmentation.service;

import com.expense.segmentation.dto.UpdateUserRequest;
import com.expense.segmentation.dto.UserResponse;
import com.expense.segmentation.exception.InvalidOperationException;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.UserMapper;
import com.expense.segmentation.model.Department;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
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
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        List<UserResponse> users =
                userRepository.findAll().stream()
                        .map(userMapper::toResponse)
                        .collect(Collectors.toList());
        log.info("Retrieved {} users", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByDepartment(UUID managerId) {
        log.debug("Fetching users by department for manager: {}", managerId);
        User manager =
                userRepository
                        .findById(managerId)
                        .orElseThrow(
                                () -> {
                                    log.error("Manager not found with id: {}", managerId);
                                    return new ResourceNotFoundException(
                                            "User", managerId.toString());
                                });

        if (manager.getDepartment() == null) {
            log.error("Manager {} is not assigned to any department", managerId);
            throw new InvalidOperationException("Manager is not assigned to any department");
        }

        UUID departmentId = manager.getDepartment().getId();
        List<UserResponse> users =
                userRepository.findByDepartmentId(departmentId).stream()
                        .map(userMapper::toResponse)
                        .collect(Collectors.toList());
        log.info("Retrieved {} users for department: {}", users.size(), departmentId);
        return users;
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        log.debug("Updating user: {} with request: {}", userId, request);
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> {
                                    log.error("User not found with id: {}", userId);
                                    return new ResourceNotFoundException("User", userId.toString());
                                });

        // Track current state before changes
        boolean wasManager = user.getRole().getName() == com.expense.segmentation.model.RoleType.MANAGER;
        Department oldDepartment = user.getDepartment();

        Department targetDepartment = null;
        boolean isBecomingManager = false;

        // Update role if provided
        if (request.getRole() != null) {
            log.debug("Updating role for user {} to {}", userId, request.getRole());
            Role role =
                    roleRepository
                            .findByName(request.getRole())
                            .orElseThrow(
                                    () -> {
                                        log.error("Role not found: {}", request.getRole());
                                        return new ResourceNotFoundException(
                                                "Role", "name", request.getRole().toString());
                                    });
            user.setRole(role);
            isBecomingManager = request.getRole() == com.expense.segmentation.model.RoleType.MANAGER;
        }

        // Update department if provided
        if (request.getDepartmentId() != null) {
            log.debug("Updating department for user {} to {}", userId, request.getDepartmentId());
            Department department =
                    departmentRepository
                            .findById(request.getDepartmentId())
                            .orElseThrow(
                                    () -> {
                                        log.error(
                                                "Department not found with id: {}",
                                                request.getDepartmentId());
                                        return new ResourceNotFoundException(
                                                "Department", request.getDepartmentId().toString());
                                    });
            user.setDepartment(department);
            targetDepartment = department;
        } else if (isBecomingManager && user.getDepartment() != null) {
            // If user is becoming a manager but department wasn't changed, use existing department
            targetDepartment = user.getDepartment();
        }

        // Handle demotion from MANAGER role
        if (wasManager && !isBecomingManager && oldDepartment != null) {
            if (oldDepartment.getManager() != null && oldDepartment.getManager().getId().equals(userId)) {
                oldDepartment.setManager(null);
                departmentRepository.save(oldDepartment);
                log.info("Removed user {} as manager of department {} due to role demotion at {}",
                    userId, oldDepartment.getId(), java.time.LocalDateTime.now());
            }
        }

        // Handle manager changing departments
        if (wasManager && request.getDepartmentId() != null && oldDepartment != null
                && !oldDepartment.getId().equals(request.getDepartmentId())) {
            // Remove manager from old department if user was its manager
            if (oldDepartment.getManager() != null && oldDepartment.getManager().getId().equals(userId)) {
                oldDepartment.setManager(null);
                departmentRepository.save(oldDepartment);
                log.info("Removed user {} as manager of department {} due to department change at {}",
                    userId, oldDepartment.getId(), java.time.LocalDateTime.now());
            }
        }

        // Handle promotion to MANAGER role
        if (isBecomingManager) {
            if (targetDepartment == null) {
                log.error("Cannot promote user {} to MANAGER role without a department", userId);
                throw new InvalidOperationException(
                    "User must be assigned to a department before being promoted to MANAGER role");
            }

            // Check if department already has a manager
            if (targetDepartment.getManager() != null
                    && !targetDepartment.getManager().getId().equals(userId)) {
                User existingManager = targetDepartment.getManager();
                log.error("Department {} already has manager {}",
                    targetDepartment.getId(), existingManager.getId());
                throw new InvalidOperationException(
                    "Department '" + targetDepartment.getName() +
                    "' already has a manager (" + existingManager.getName() +
                    "). Please demote the current manager first.");
            }

            // Set user as department manager
            log.debug("Setting user {} as manager of department {}", userId, targetDepartment.getId());
            targetDepartment.setManager(user);
            departmentRepository.save(targetDepartment);
            log.info("Successfully promoted user {} to manager of department {} at {}",
                userId, targetDepartment.getId(), java.time.LocalDateTime.now());
        }

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated user: {}", userId);
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        log.debug("Deactivating user: {}", userId);
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> {
                                    log.error("User not found with id: {}", userId);
                                    return new ResourceNotFoundException("User", userId.toString());
                                });

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("Successfully deactivated user: {}", userId);
    }

    /**
     * Gets users in the same department as the currently authenticated manager.
     *
     * @param email the email of the authenticated manager
     * @return list of users in the manager's department
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByAuthenticatedManager(String email) {
        log.debug("Fetching users for authenticated manager: {}", email);
        User manager =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> {
                                    log.error("Manager not found with email: {}", email);
                                    return new ResourceNotFoundException("User", "email", email);
                                });

        return getUsersByDepartment(manager.getId());
    }
}
