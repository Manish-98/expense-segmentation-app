package com.expense.segmentation.dto;

import com.expense.segmentation.model.RoleType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating user information. At least one field must be provided. Both fields are optional
 * to allow partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    /** The new role for the user. Optional. */
    private RoleType role;

    /** The new department ID for the user. Optional. */
    private UUID departmentId;

    /**
     * Validates that at least one field is provided.
     *
     * @return true if at least one field is non-null
     */
    public boolean hasAtLeastOneField() {
        return role != null || departmentId != null;
    }
}
