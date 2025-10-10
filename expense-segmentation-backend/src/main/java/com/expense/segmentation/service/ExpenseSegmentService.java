package com.expense.segmentation.service;

import com.expense.segmentation.dto.CreateExpenseSegmentRequest;
import com.expense.segmentation.dto.CreateMultipleExpenseSegmentsRequest;
import com.expense.segmentation.dto.ExpenseSegmentResponse;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.exception.SegmentAmountExceedsExpenseException;
import com.expense.segmentation.exception.SegmentValidationException;
import com.expense.segmentation.mapper.ExpenseSegmentMapper;
import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseSegment;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.ExpenseSegmentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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

    @Transactional
    public List<ExpenseSegmentResponse> addExpenseSegment(
            UUID expenseId, CreateExpenseSegmentRequest request) {
        log.debug("Adding segment to expense ID: {}", expenseId);

        // Verify expense exists
        Expense expense =
                expenseRepository
                        .findById(expenseId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Expense not found with ID: " + expenseId));

        // Validate segment amount doesn't exceed expense amount
        validateSegmentAmount(expense.getAmount(), request.getAmount());

        // Check if segments already exist for this expense
        List<ExpenseSegment> existingSegments =
                expenseSegmentRepository.findByExpenseIdOrderByCategory(expenseId);
        if (!existingSegments.isEmpty()) {
            throw new SegmentValidationException(
                    "Expense already has segments. Use update endpoint to modify existing"
                            + " segments.");
        }

        // Calculate percentage if not provided
        BigDecimal percentage = request.getPercentage();
        if (percentage == null) {
            percentage = calculatePercentage(request.getAmount(), expense.getAmount());
        }

        // Create and save the segment
        ExpenseSegment segment = new ExpenseSegment();
        segment.setExpense(expense);
        segment.setCategory(request.getCategory().trim());
        segment.setAmount(request.getAmount());
        segment.setPercentage(percentage);

        segment = expenseSegmentRepository.save(segment);
        log.info("Created segment: {} for expense: {}", segment.getId(), expenseId);

        return expenseSegmentMapper.toResponseList(List.of(segment));
    }

    @Transactional
    public List<ExpenseSegmentResponse> addMultipleExpenseSegments(
            UUID expenseId, CreateMultipleExpenseSegmentsRequest request) {
        log.debug("Adding multiple segments to expense ID: {}", expenseId);

        // Verify expense exists
        Expense expense =
                expenseRepository
                        .findById(expenseId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Expense not found with ID: " + expenseId));

        // Validate total segments amount equals expense amount
        validateTotalSegmentsAmount(expense.getAmount(), request.getSegments());

        // Check for duplicate categories
        validateUniqueCategories(request.getSegments());

        // Delete existing segments if any
        expenseSegmentRepository.deleteByExpenseId(expenseId);

        List<ExpenseSegment> segments = new ArrayList<>();
        for (CreateExpenseSegmentRequest segmentRequest : request.getSegments()) {
            // Calculate percentage if not provided
            BigDecimal percentage = segmentRequest.getPercentage();
            if (percentage == null) {
                percentage = calculatePercentage(segmentRequest.getAmount(), expense.getAmount());
            }

            ExpenseSegment segment = new ExpenseSegment();
            segment.setExpense(expense);
            segment.setCategory(segmentRequest.getCategory().trim());
            segment.setAmount(segmentRequest.getAmount());
            segment.setPercentage(percentage);

            segments.add(segment);
        }

        // Save all segments
        List<ExpenseSegment> savedSegments = expenseSegmentRepository.saveAll(segments);
        log.info("Created {} segments for expense: {}", savedSegments.size(), expenseId);

        return expenseSegmentMapper.toResponseList(savedSegments);
    }

    @Transactional
    public List<ExpenseSegmentResponse> replaceAllExpenseSegments(
            UUID expenseId, CreateMultipleExpenseSegmentsRequest request) {
        log.debug("Replacing all segments for expense ID: {}", expenseId);
        return addMultipleExpenseSegments(expenseId, request);
    }

    private void validateSegmentAmount(BigDecimal expenseAmount, BigDecimal segmentAmount) {
        if (segmentAmount.compareTo(expenseAmount) > 0) {
            throw new SegmentAmountExceedsExpenseException(segmentAmount, expenseAmount);
        }
    }

    private void validateTotalSegmentsAmount(
            BigDecimal expenseAmount, List<CreateExpenseSegmentRequest> segments) {
        BigDecimal totalSegmentsAmount =
                segments.stream()
                        .map(CreateExpenseSegmentRequest::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Allow small difference for rounding (0.01)
        BigDecimal difference = totalSegmentsAmount.subtract(expenseAmount).abs();
        if (difference.compareTo(new BigDecimal("0.01")) > 0) {
            throw new SegmentValidationException(
                    String.format(
                            "Total segments amount (%s) must equal expense amount (%s)",
                            totalSegmentsAmount.toPlainString(), expenseAmount.toPlainString()));
        }
    }

    private void validateUniqueCategories(List<CreateExpenseSegmentRequest> segments) {
        long uniqueCategories =
                segments.stream()
                        .map(segment -> segment.getCategory().trim().toLowerCase())
                        .distinct()
                        .count();

        if (uniqueCategories != segments.size()) {
            throw new SegmentValidationException(
                    "Segment categories must be unique within an expense");
        }
    }

    private BigDecimal calculatePercentage(BigDecimal segmentAmount, BigDecimal totalAmount) {
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return segmentAmount
                .multiply(new BigDecimal("100"))
                .divide(totalAmount, 2, RoundingMode.HALF_UP);
    }
}
