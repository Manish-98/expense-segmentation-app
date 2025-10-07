package com.expense.segmentation.dto;

import com.expense.segmentation.model.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private UUID id;
    private RoleType name;
    private String description;
}
