package com.expense.segmentation.dto;

import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private RoleType role;
    private String departmentName;
    private UUID departmentId;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
