package com.expense.segmentation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateExpenseSegmentRequest {

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 16, fraction = 2, message = "Amount must have at most 2 decimal places")
    @DecimalMax(value = "999999999999.99", message = "Amount must not exceed 999,999,999,999.99")
    private BigDecimal amount;

    @Digits(integer = 3, fraction = 2, message = "Percentage must have at most 2 decimal places")
    @DecimalMin(value = "0.00", message = "Percentage must be non-negative")
    @DecimalMax(value = "100.00", message = "Percentage must not exceed 100")
    private BigDecimal percentage;
}
