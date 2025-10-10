package com.expense.segmentation.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSegmentResponse {

    private UUID id;
    private String category;
    private BigDecimal amount;
    private BigDecimal percentage;
}
