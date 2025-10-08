package com.expense.segmentation.mapper;

import com.expense.segmentation.dto.ExpenseResponse;
import com.expense.segmentation.model.Expense;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting Expense entities to ExpenseResponse DTOs. Centralizes the mapping
 * logic to avoid duplication across service classes.
 */
@Component
public class ExpenseMapper {

    /**
     * Converts an Expense entity to an ExpenseResponse DTO.
     *
     * @param expense the expense entity to convert
     * @return the converted ExpenseResponse DTO
     */
    public ExpenseResponse toResponse(Expense expense) {
        if (expense == null) {
            return null;
        }

        return ExpenseResponse.builder()
                .id(expense.getId())
                .date(expense.getDate())
                .vendor(expense.getVendor())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .type(expense.getType())
                .createdById(expense.getCreatedBy() != null ? expense.getCreatedBy().getId() : null)
                .createdByName(
                        expense.getCreatedBy() != null ? expense.getCreatedBy().getName() : null)
                .createdByEmail(
                        expense.getCreatedBy() != null ? expense.getCreatedBy().getEmail() : null)
                .status(expense.getStatus())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
