package com.expense.segmentation.service;

import com.expense.segmentation.dto.ExpenseSegmentResponse;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.ExpenseSegmentMapper;
import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseSegment;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.ExpenseSegmentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseSegmentService {

    private final ExpenseSegmentRepository expenseSegmentRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSegmentMapper expenseSegmentMapper;

    public List<ExpenseSegmentResponse> getSegmentsByExpenseId(UUID expenseId) {
        log.debug("Fetching segments for expense ID: {}", expenseId);

        // Verify expense exists
        Expense expense =
                expenseRepository
                        .findById(expenseId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Expense not found with ID: " + expenseId));

        List<ExpenseSegment> segments =
                expenseSegmentRepository.findByExpenseIdOrderByCategory(expenseId);

        // If no segments exist, return mock data for now
        if (segments.isEmpty()) {
            log.debug("No segments found for expense ID: {}, returning mock data", expenseId);
            return createMockSegments(expense);
        }

        return expenseSegmentMapper.toResponseList(segments);
    }

    private List<ExpenseSegmentResponse> createMockSegments(Expense expense) {
        BigDecimal totalAmount = expense.getAmount();

        // Create mock segments with typical business expense categories
        return List.of(
                new ExpenseSegmentResponse(
                        UUID.randomUUID(),
                        "Travel",
                        totalAmount.multiply(new BigDecimal("0.40")),
                        new BigDecimal("40.00")),
                new ExpenseSegmentResponse(
                        UUID.randomUUID(),
                        "Meals",
                        totalAmount.multiply(new BigDecimal("0.30")),
                        new BigDecimal("30.00")),
                new ExpenseSegmentResponse(
                        UUID.randomUUID(),
                        "Supplies",
                        totalAmount.multiply(new BigDecimal("0.20")),
                        new BigDecimal("20.00")),
                new ExpenseSegmentResponse(
                        UUID.randomUUID(),
                        "Other",
                        totalAmount.multiply(new BigDecimal("0.10")),
                        new BigDecimal("10.00")));
    }
}
