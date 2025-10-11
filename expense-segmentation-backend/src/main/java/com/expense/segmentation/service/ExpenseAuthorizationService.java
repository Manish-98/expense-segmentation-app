package com.expense.segmentation.service;

import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseAuthorizationService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public boolean canModifyExpense(UUID expenseId, String username) {
        log.debug(
                "Checking modification permission for expense {} by user {}", expenseId, username);

        try {
            Expense expense =
                    expenseRepository
                            .findById(expenseId)
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Expense not found with ID: " + expenseId));

            User user =
                    userRepository
                            .findByEmail(username)
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "User not found with email: " + username));

            // Users can modify their own expenses
            if (expense.getCreatedBy().getId().equals(user.getId())) {
                log.debug("User {} can modify expense {} (owner)", username, expenseId);
                return true;
            }

            // Admin and Finance can modify any expense
            if (hasRole(user, RoleType.ADMIN) || hasRole(user, RoleType.FINANCE)) {
                log.debug("User {} can modify expense {} (admin/finance)", username, expenseId);
                return true;
            }

            log.debug("User {} cannot modify expense {} (no permission)", username, expenseId);
            return false;

        } catch (ResourceNotFoundException e) {
            log.warn("Authorization check failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean canViewExpense(UUID expenseId, String username) {
        log.debug("Checking view permission for expense {} by user {}", expenseId, username);

        try {
            Expense expense =
                    expenseRepository
                            .findById(expenseId)
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Expense not found with ID: " + expenseId));

            User user =
                    userRepository
                            .findByEmail(username)
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "User not found with email: " + username));

            // Users can view their own expenses
            if (expense.getCreatedBy().getId().equals(user.getId())) {
                log.debug("User {} can view expense {} (owner)", username, expenseId);
                return true;
            }

            // Manager, Finance, and Admin can view any expense
            if (hasRole(user, RoleType.MANAGER)
                    || hasRole(user, RoleType.FINANCE)
                    || hasRole(user, RoleType.ADMIN)) {
                log.debug(
                        "User {} can view expense {} (manager/finance/admin)", username, expenseId);
                return true;
            }

            log.debug("User {} cannot view expense {} (no permission)", username, expenseId);
            return false;

        } catch (ResourceNotFoundException e) {
            log.warn("Authorization check failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean canModifySegments(UUID expenseId, String username) {
        // Segment modification follows the same rules as expense modification
        return canModifyExpense(expenseId, username);
    }

    public boolean canManageCategories(String username) {
        log.debug("Checking category management permission for user {}", username);

        try {
            User user =
                    userRepository
                            .findByEmail(username)
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "User not found with email: " + username));

            // Only Manager, Finance, and Admin can manage categories
            boolean canManage =
                    hasRole(user, RoleType.MANAGER)
                            || hasRole(user, RoleType.FINANCE)
                            || hasRole(user, RoleType.ADMIN);

            log.debug("User {} {} manage categories", username, canManage ? "can" : "cannot");
            return canManage;

        } catch (ResourceNotFoundException e) {
            log.warn("Authorization check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean hasRole(User user, RoleType roleType) {
        return user.getRole() != null && user.getRole().getName() == roleType;
    }
}
