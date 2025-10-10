package com.expense.segmentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMultipleExpenseSegmentsRequest {

    @NotEmpty(message = "At least one segment is required")
    @Size(max = 20, message = "Cannot create more than 20 segments at once")
    @Valid
    private List<CreateExpenseSegmentRequest> segments;
}
