package com.expense.segmentation.service;

import com.expense.segmentation.dto.UpdateUserRequest;
import com.expense.segmentation.dto.UserResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByDepartment(UUID managerId) {
        User manager =
                userRepository
                        .findById(managerId)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Manager not found with id: " + managerId));

        if (manager.getDepartment() == null) {
            throw new RuntimeException("Manager is not assigned to any department");
        }

        return userRepository.findByDepartmentId(manager.getDepartment().getId()).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new RuntimeException("User not found with id: " + userId));

        // Update role if provided
        if (request.getRole() != null) {
            Role role =
                    roleRepository
                            .findByName(request.getRole())
                            .orElseThrow(
                                    () ->
                                            new RuntimeException(
                                                    "Role not found: " + request.getRole()));
            user.setRole(role);
        }

        // Update department if provided
        if (request.getDepartmentId() != null) {
            Department department =
                    departmentRepository
                            .findById(request.getDepartmentId())
                            .orElseThrow(
                                    () ->
                                            new RuntimeException(
                                                    "Department not found with id: "
                                                            + request.getDepartmentId()));
            user.setDepartment(department);
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new RuntimeException("User not found with id: " + userId));

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().getName());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        if (user.getDepartment() != null) {
            response.setDepartmentId(user.getDepartment().getId());
            response.setDepartmentName(user.getDepartment().getName());
        }

        return response;
    }
}
