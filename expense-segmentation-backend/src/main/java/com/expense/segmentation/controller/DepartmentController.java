package com.expense.segmentation.controller;

import com.expense.segmentation.dto.CreateDepartmentRequest;
import com.expense.segmentation.dto.DepartmentResponse;
import com.expense.segmentation.dto.UpdateDepartmentRequest;
import com.expense.segmentation.service.DepartmentService;
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
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        log.info("POST /departments - Admin creating department: {}", request.getCode());
        DepartmentResponse response = departmentService.createDepartment(request);
        log.info("POST /departments - Department created successfully: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        log.info("GET /departments - Retrieving all departments");
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable UUID id) {
        log.info("GET /departments/{} - Retrieving department", id);
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable UUID id, @Valid @RequestBody UpdateDepartmentRequest request) {
        log.info("PATCH /departments/{} - Admin updating department", id);
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        log.info("PATCH /departments/{} - Department updated successfully", id);
        return ResponseEntity.ok(response);
    }
}
