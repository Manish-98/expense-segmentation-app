package com.expense.segmentation.dto;

import com.expense.segmentation.model.RoleType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private UUID id;
    private RoleType name;
    private String description;
}
