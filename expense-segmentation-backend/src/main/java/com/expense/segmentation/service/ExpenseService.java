package com.expense.segmentation.service;

import com.expense.segmentation.dto.CreateExpenseRequest;
import com.expense.segmentation.dto.ExpenseResponse;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.ExpenseMapper;
import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseStatus;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        log.info("Creating new expense/invoice of type: {}", request.getType());

        // Get authenticated user
        User currentUser = getCurrentUser();

        // Build expense entity
        Expense expense = buildExpense(request, currentUser);

        // Save to database
        Expense saved = expenseRepository.save(expense);

        log.info(
                "Successfully created expense: {} by user: {}",
                saved.getId(),
                currentUser.getEmail());
        return expenseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        log.debug("Fetching all expenses");
        List<ExpenseResponse> expenses =
                expenseRepository.findAllWithCreatedBy().stream()
                        .map(expenseMapper::toResponse)
                        .toList();
        log.info("Retrieved {} expenses", expenses.size());
        return expenses;
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(UUID id) {
        log.debug("Fetching expense with id: {}", id);
        Expense expense = findExpenseByIdWithCreatedBy(id);
        return expenseMapper.toResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByUser(UUID userId) {
        log.debug("Fetching expenses for user: {}", userId);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            log.error("User not found with id: {}", userId);
            throw new ResourceNotFoundException("User", userId.toString());
        }

        // Get current authenticated user
        User currentUser = getCurrentUser();

        // Authorization check: Users can only view their own expenses
        // unless they have FINANCE or ADMIN roles
        RoleType currentUserRole = currentUser.getRole().getName();
        boolean isFinanceOrAdmin =
                RoleType.FINANCE.equals(currentUserRole) || RoleType.ADMIN.equals(currentUserRole);

        if (!isFinanceOrAdmin && !currentUser.getId().equals(userId)) {
            log.warn(
                    "User {} attempted to access expenses of user {} without permission",
                    currentUser.getId(),
                    userId);
            throw new SecurityException("You are not authorized to view expenses for this user");
        }

        List<ExpenseResponse> expenses =
                expenseRepository.findByCreatedById(userId).stream()
                        .map(expenseMapper::toResponse)
                        .toList();
        log.info("Retrieved {} expenses for user: {}", expenses.size(), userId);
        return expenses;
    }

    private Expense buildExpense(CreateExpenseRequest request, User currentUser) {
        Expense expense = new Expense();

        // Set date - default to current date if not provided
        expense.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());

        expense.setVendor(request.getVendor());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setType(request.getType());
        expense.setCreatedBy(currentUser);
        expense.setStatus(ExpenseStatus.SUBMITTED);

        return expense;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        return userRepository
                .findByEmail(userEmail)
                .orElseThrow(
                        () -> {
                            log.error("Authenticated user not found: {}", userEmail);
                            return new ResourceNotFoundException("User", "email", userEmail);
                        });
    }

    private Expense findExpenseByIdWithCreatedBy(UUID id) {
        return expenseRepository
                .findByIdWithCreatedBy(id)
                .orElseThrow(
                        () -> {
                            log.error("Expense not found with id: {}", id);
                            return new ResourceNotFoundException("Expense", id.toString());
                        });
    }
}
