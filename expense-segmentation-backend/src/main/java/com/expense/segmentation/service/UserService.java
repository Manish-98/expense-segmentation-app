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
