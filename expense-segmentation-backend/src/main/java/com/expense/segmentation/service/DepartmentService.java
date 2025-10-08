package com.expense.segmentation.service;

import com.expense.segmentation.dto.CreateDepartmentRequest;
import com.expense.segmentation.dto.DepartmentResponse;
import com.expense.segmentation.dto.UpdateDepartmentRequest;
import com.expense.segmentation.exception.DuplicateResourceException;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.DepartmentMapper;
import com.expense.segmentation.model.Department;
import com.expense.segmentation.model.User;
import com.expense.segmentation.repository.DepartmentRepository;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.info("Creating new department with code: {}", request.getCode());

        // Check if department code already exists
        if (departmentRepository.existsByCode(request.getCode())) {
            log.error("Department creation failed: code {} already exists", request.getCode());
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());

        // Set manager if provided
        if (request.getManagerId() != null) {
            log.debug("Setting manager {} for department {}", request.getManagerId(), request.getCode());
            User manager =
                    userRepository
                            .findById(request.getManagerId())
                            .orElseThrow(
                                    () -> {
                                        log.error("Manager not found with id: {}", request.getManagerId());
                                        return new ResourceNotFoundException(
                                                "User", request.getManagerId().toString());
                                    });
            department.setManager(manager);
        }

        Department saved = departmentRepository.save(department);
        log.info("Successfully created department: {} with id: {}", saved.getCode(), saved.getId());
        return departmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        log.debug("Fetching all departments");
        List<DepartmentResponse> departments = departmentRepository.findAll().stream()
                .map(departmentMapper::toResponse)
                .collect(Collectors.toList());
        log.info("Retrieved {} departments", departments.size());
        return departments;
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(UUID id) {
        log.debug("Fetching department with id: {}", id);
        Department department =
                departmentRepository
                        .findById(id)
                        .orElseThrow(
                                () -> {
                                    log.error("Department not found with id: {}", id);
                                    return new ResourceNotFoundException("Department", id.toString());
                                });
        return departmentMapper.toResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(UUID id, UpdateDepartmentRequest request) {
        log.info("Updating department: {}", id);
        Department department =
                departmentRepository
                        .findById(id)
                        .orElseThrow(
                                () -> {
                                    log.error("Department not found with id: {}", id);
                                    return new ResourceNotFoundException("Department", id.toString());
                                });

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            log.debug("Updating department {} name to: {}", id, request.getName());
            department.setName(request.getName());
        }

        // Update manager if provided
        if (request.getManagerId() != null) {
            log.debug("Updating department {} manager to: {}", id, request.getManagerId());
            User manager =
                    userRepository
                            .findById(request.getManagerId())
                            .orElseThrow(
                                    () -> {
                                        log.error("Manager not found with id: {}", request.getManagerId());
                                        return new ResourceNotFoundException(
                                                "User", request.getManagerId().toString());
                                    });
            department.setManager(manager);
        }

        Department updated = departmentRepository.save(department);
        log.info("Successfully updated department: {}", id);
        return departmentMapper.toResponse(updated);
    }
}
