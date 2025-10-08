package com.expense.segmentation.mapper;

import com.expense.segmentation.dto.DepartmentResponse;
import com.expense.segmentation.model.Department;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting Department entities to DepartmentResponse DTOs. Centralizes the
 * mapping logic to avoid duplication across service classes.
 */
@Component
public class DepartmentMapper {

    /**
     * Converts a Department entity to a DepartmentResponse DTO.
     *
     * @param department the department entity to convert
     * @return the converted DepartmentResponse DTO
     */
    public DepartmentResponse toResponse(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setCode(department.getCode());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());

        if (department.getManager() != null) {
            response.setManagerId(department.getManager().getId());
            response.setManagerName(department.getManager().getName());
        }

        return response;
    }
}
