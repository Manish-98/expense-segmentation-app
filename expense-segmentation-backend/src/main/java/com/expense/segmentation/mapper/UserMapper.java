package com.expense.segmentation.mapper;

import com.expense.segmentation.dto.UserResponse;
import com.expense.segmentation.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting User entities to UserResponse DTOs. Centralizes the mapping logic to
 * avoid duplication across service classes.
 */
@Component
public class UserMapper {

    /**
     * Converts a User entity to a UserResponse DTO.
     *
     * @param user the user entity to convert
     * @return the converted UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().getName());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        if (user.getDepartment() != null) {
            response.setDepartmentId(user.getDepartment().getId());
            response.setDepartmentName(user.getDepartment().getName());
        }

        return response;
    }
}
