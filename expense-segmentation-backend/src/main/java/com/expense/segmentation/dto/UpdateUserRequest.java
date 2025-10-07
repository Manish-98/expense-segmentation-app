package com.expense.segmentation.dto;

import com.expense.segmentation.model.RoleType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private RoleType role;
    private UUID departmentId;
}
