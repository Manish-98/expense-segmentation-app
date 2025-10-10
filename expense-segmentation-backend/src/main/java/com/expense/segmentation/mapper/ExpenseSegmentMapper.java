package com.expense.segmentation.mapper;

import com.expense.segmentation.dto.ExpenseSegmentResponse;
import com.expense.segmentation.model.ExpenseSegment;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ExpenseSegmentMapper {

    public ExpenseSegmentResponse toResponse(ExpenseSegment expenseSegment) {
        if (expenseSegment == null) {
            return null;
        }

        ExpenseSegmentResponse response = new ExpenseSegmentResponse();
        response.setId(expenseSegment.getId());
        response.setCategory(expenseSegment.getCategory());
        response.setAmount(expenseSegment.getAmount());
        response.setPercentage(expenseSegment.getPercentage());
        return response;
    }

    public List<ExpenseSegmentResponse> toResponseList(List<ExpenseSegment> expenseSegments) {
        if (expenseSegments == null) {
            return List.of();
        }
        return expenseSegments.stream().map(this::toResponse).toList();
    }
}
