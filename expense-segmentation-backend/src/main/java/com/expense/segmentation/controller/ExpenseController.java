package com.expense.segmentation.controller;

import com.expense.segmentation.dto.CreateExpenseRequest;
import com.expense.segmentation.dto.ExpenseResponse;
import com.expense.segmentation.dto.PagedExpenseResponse;
import com.expense.segmentation.model.ExpenseStatus;
import com.expense.segmentation.model.ExpenseType;
import com.expense.segmentation.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN')")
    @Operation(
            summary = "Get expenses with pagination and filters",
            description =
                    "Retrieves expenses with pagination and optional filters. Employees see only"
                            + " their own expenses. Finance and Admin see all expenses.")
    public ResponseEntity<PagedExpenseResponse> getExpenses(
            @Parameter(description = "Page number (0-indexed)", example = "0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10")
                    int size,
            @Parameter(description = "Filter by date from (inclusive)", example = "2024-01-01")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate dateFrom,
            @Parameter(description = "Filter by date to (inclusive)", example = "2024-12-31")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate dateTo,
            @Parameter(description = "Filter by expense type", example = "EXPENSE")
                    @RequestParam(required = false)
                    ExpenseType type,
            @Parameter(description = "Filter by status", example = "SUBMITTED")
                    @RequestParam(required = false)
                    ExpenseStatus status) {
        log.info(
                "GET /expenses - page: {}, size: {}, dateFrom: {}, dateTo: {}, type: {}, status:"
                        + " {}",
                page,
                size,
                dateFrom,
                dateTo,
                type,
                status);
        return ResponseEntity.ok(
                expenseService.getExpensesWithFilters(page, size, dateFrom, dateTo, type, status));
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
                    "Retrieves expenses for a specific user. Users can only view their own expenses"
                            + " unless they have FINANCE or ADMIN role")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByUser(@PathVariable UUID userId) {
        log.info("GET /expenses/user/{} - Retrieving expenses for user", userId);
        return ResponseEntity.ok(expenseService.getExpensesByUser(userId));
    }
}
