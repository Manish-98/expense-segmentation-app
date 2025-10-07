package com.expense.segmentation.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartmentRequest {

    @Size(max = 100, message = "Department name must not exceed 100 characters")
    private String name;

    private UUID managerId;
}
