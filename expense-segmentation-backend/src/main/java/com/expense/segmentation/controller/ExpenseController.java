package com.expense.segmentation.controller;

import com.expense.segmentation.dto.CreateExpenseRequest;
import com.expense.segmentation.dto.ExpenseResponse;
import com.expense.segmentation.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Expense Management", description = "Expense and invoice management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN')")
    @Operation(
            summary = "Create expense or invoice",
            description = "Allows authenticated users to submit new expenses or invoices")
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request) {
        log.info("POST /expenses - Creating expense/invoice of type: {}", request.getType());
        ExpenseResponse response = expenseService.createExpense(request);
        log.info("POST /expenses - Expense created successfully: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FINANCE', 'ADMIN')")
    @Operation(
            summary = "Get all expenses",
            description = "Finance and Admin only - retrieves all expenses and invoices")
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        log.info("GET /expenses - Retrieving all expenses");
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN')")
    @Operation(
            summary = "Get expense by ID",
            description = "Retrieves a specific expense or invoice by its ID")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable UUID id) {
        log.info("GET /expenses/{} - Retrieving expense", id);
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'FINANCE', 'ADMIN')")
    @Operation(
            summary = "Get expenses by user",
            description =
                    "Retrieves expenses for a specific user. Users can only view their own expenses unless they have FINANCE or ADMIN role")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByUser(@PathVariable UUID userId) {
        log.info("GET /expenses/user/{} - Retrieving expenses for user", userId);
        return ResponseEntity.ok(expenseService.getExpensesByUser(userId));
    }
}
