package com.expense.segmentation.controller;

import com.expense.segmentation.dto.CategoryResponse;
import com.expense.segmentation.dto.CreateCategoryRequest;
import com.expense.segmentation.service.CategoryService;
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
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "Category management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN')")
    @Operation(
            summary = "Get all active categories",
            description = "Retrieves all active expense categories sorted by name")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        log.info("GET /categories - Fetching all active categories");
        List<CategoryResponse> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    @PreAuthorize("@expenseAuthorizationService.canManageCategories(authentication.name)")
    @Operation(
            summary = "Create new category",
            description =
                    "Creates a new expense category. Only managers, finance, and admin users can"
                            + " create categories.")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        log.info("POST /categories - Creating new category: {}", request.getName());
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@expenseAuthorizationService.canManageCategories(authentication.name)")
    @Operation(
            summary = "Deactivate category",
            description =
                    "Deactivates a category (soft delete). Only managers, finance, and admin users"
                            + " can deactivate categories.")
    public ResponseEntity<Void> deactivateCategory(@PathVariable UUID id) {
        log.info("DELETE /categories/{} - Deactivating category", id);
        categoryService.deactivateCategory(id);
        return ResponseEntity.noContent().build();
    }
}
