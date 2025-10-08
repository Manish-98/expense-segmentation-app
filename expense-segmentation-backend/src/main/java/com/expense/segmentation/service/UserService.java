package com.expense.segmentation.service;

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
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final UserMapper userMapper;

    public UserService(
            UserRepository userRepository,
            RoleService roleService,
            @Lazy DepartmentService departmentService,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users with department and role");
        List<UserResponse> users =
                userRepository.findAllWithDepartmentAndRole().stream()
                        .map(userMapper::toResponse)
                        .toList();
        log.info("Retrieved {} users", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByDepartment(UUID managerId) {
        log.debug("Fetching users by department for manager: {}", managerId);
        User manager = findUserById(managerId);

        if (manager.getDepartment() == null) {
            log.error("Manager {} is not assigned to any department", managerId);
            throw new InvalidOperationException("Manager is not assigned to any department");
        }

        UUID departmentId = manager.getDepartment().getId();
        List<UserResponse> users =
                userRepository.findByDepartmentIdWithRole(departmentId).stream()
                        .map(userMapper::toResponse)
                        .toList();
        log.info("Retrieved {} users for department: {}", users.size(), departmentId);
        return users;
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        log.debug("Updating user: {} with request: {}", userId, request);
        User user = findUserById(userId);

        boolean wasManager = isManager(user);
        Department oldDepartment = user.getDepartment();

        boolean isBecomingManager = updateUserRole(user, request);
        updateUserDepartment(user, request);

        Department targetDepartment = determineTargetDepartment(user, request, isBecomingManager);

        handleManagerDemotion(user, userId, wasManager, isBecomingManager, oldDepartment);
        handleManagerDepartmentChange(userId, wasManager, request, oldDepartment, targetDepartment);
        handleManagerPromotion(user, userId, isBecomingManager, targetDepartment);

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated user: {}", userId);
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        log.debug("Deactivating user: {}", userId);
        User user = findUserById(userId);

        if (isManager(user) && user.getDepartment() != null) {
            removeUserAsManagerFromDepartment(userId, user.getDepartment());
        }

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
        User manager = findUserByEmail(email);
        return getUsersByDepartment(manager.getId());
    }

    /**
     * Gets a user entity by ID. Used internally by other services.
     *
     * @param userId the user ID
     * @return the user entity
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(UUID userId) {
        return findUserById(userId);
    }

    /**
     * Updates a user entity. Used internally by other services for manager assignment. Uses
     * MANDATORY propagation to ensure it runs within the caller's transaction.
     *
     * @param user the user to update
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void updateUserEntity(User user) {
        log.debug("Updating user entity: {}", user.getId());
        userRepository.save(user);
    }

    private boolean updateUserRole(User user, UpdateUserRequest request) {
        if (request.getRole() == null) {
            return false;
        }

        log.debug("Updating role for user {} to {}", user.getId(), request.getRole());
        Role role = roleService.getRoleByName(request.getRole());
        user.setRole(role);
        return RoleType.MANAGER.equals(request.getRole());
    }

    private void updateUserDepartment(User user, UpdateUserRequest request) {
        if (request.getDepartmentId() == null) {
            return;
        }

        log.debug("Updating department for user {} to {}", user.getId(), request.getDepartmentId());
        Department department =
                departmentService.getDepartmentEntityById(request.getDepartmentId());
        user.setDepartment(department);
    }

    private Department determineTargetDepartment(
            User user, UpdateUserRequest request, boolean isBecomingManager) {
        if (request.getDepartmentId() != null) {
            return user.getDepartment();
        }

        boolean willBeManager = isBecomingManager || isManager(user);
        return (willBeManager && user.getDepartment() != null) ? user.getDepartment() : null;
    }

    private void handleManagerDemotion(
            User user,
            UUID userId,
            boolean wasManager,
            boolean isBecomingManager,
            Department oldDepartment) {
        if (!wasManager || isBecomingManager || oldDepartment == null) {
            return;
        }

        // User is being demoted from MANAGER role
        removeUserAsManagerFromDepartment(userId, oldDepartment);
    }

    private void handleManagerDepartmentChange(
            UUID userId,
            boolean wasManager,
            UpdateUserRequest request,
            Department oldDepartment,
            Department targetDepartment) {
        if (!wasManager
                || request.getDepartmentId() == null
                || oldDepartment == null
                || targetDepartment == null) {
            return;
        }

        if (oldDepartment.getId().equals(targetDepartment.getId())) {
            return;
        }

        // Manager is changing departments
        removeUserAsManagerFromDepartment(userId, oldDepartment);
    }

    private void handleManagerPromotion(
            User user, UUID userId, boolean isBecomingManager, Department targetDepartment) {
        boolean isManagerAfterUpdate = isManager(user);

        if (!isManagerAfterUpdate || targetDepartment == null) {
            if (isBecomingManager && targetDepartment == null) {
                log.error("Cannot promote user {} to MANAGER role without a department", userId);
                throw new InvalidOperationException(
                        "User must be assigned to a department before being promoted to MANAGER"
                                + " role");
            }
            return;
        }

        validateDepartmentHasNoOtherManager(userId, targetDepartment);
        setUserAsManagerOfDepartment(user, userId, targetDepartment);
    }

    private void validateDepartmentHasNoOtherManager(UUID userId, Department department) {
        User currentManager = department.getManager();
        if (currentManager != null && !currentManager.getId().equals(userId)) {
            log.error(
                    "Department {} already has manager {}",
                    department.getId(),
                    currentManager.getId());
            throw new InvalidOperationException(
                    "Department '"
                            + department.getName()
                            + "' already has a manager ("
                            + currentManager.getName()
                            + "). Please demote the current manager first.");
        }
    }

    private void setUserAsManagerOfDepartment(User user, UUID userId, Department department) {
        if (department.getManager() != null && department.getManager().getId().equals(userId)) {
            return; // User is already the manager
        }

        log.debug("Setting user {} as manager of department {}", userId, department.getId());
        department.setManager(user);
        departmentService.updateDepartmentEntity(department);
        log.info(
                "Successfully set user {} as manager of department {}", userId, department.getId());
    }

    private void removeUserAsManagerFromDepartment(UUID userId, Department department) {
        if (department.getManager() == null || !department.getManager().getId().equals(userId)) {
            return;
        }

        department.setManager(null);
        departmentService.updateDepartmentEntity(department);
        log.info("Removed user {} as manager of department {}", userId, department.getId());
    }

    private boolean isManager(User user) {
        return user.getRole() != null && RoleType.MANAGER.equals(user.getRole().getName());
    }

    private User findUserById(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(
                        () -> {
                            log.error("User not found with id: {}", id);
                            return new ResourceNotFoundException("User", id.toString());
                        });
    }

    private User findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> {
                            log.error("User not found with email: {}", email);
                            return new ResourceNotFoundException("User", "email", email);
                        });
    }
}
