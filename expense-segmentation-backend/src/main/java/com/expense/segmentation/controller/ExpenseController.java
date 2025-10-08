package com.expense.segmentation.controller;

import com.expense.segmentation.dto.CreateExpenseRequest;
import com.expense.segmentation.dto.ExpenseResponse;
import com.expense.segmentation.service.ExpenseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN')")
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request) {
        log.info("POST /expenses - Creating expense/invoice of type: {}", request.getType());
        ExpenseResponse response = expenseService.createExpense(request);
        log.info("POST /expenses - Expense created successfully: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FINANCE', 'ADMIN')")
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        log.info("GET /expenses - Retrieving all expenses");
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN')")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable UUID id) {
        log.info("GET /expenses/{} - Retrieving expense", id);
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE', 'ADMIN')")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByUser(@PathVariable UUID userId) {
        log.info("GET /expenses/user/{} - Retrieving expenses for user", userId);
        return ResponseEntity.ok(expenseService.getExpensesByUser(userId));
    }
}
