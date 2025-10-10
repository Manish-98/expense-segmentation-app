package com.expense.segmentation.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when segment amount exceeds the expense amount. This typically results in an
 * HTTP 400 Bad Request response.
 */
public class SegmentAmountExceedsExpenseException extends SegmentValidationException {

    public SegmentAmountExceedsExpenseException(
            BigDecimal segmentAmount, BigDecimal expenseAmount) {
        super(
                String.format(
                        "Segment amount (%s) exceeds expense amount (%s)",
                        segmentAmount.toPlainString(), expenseAmount.toPlainString()));
    }

    public SegmentAmountExceedsExpenseException(String message) {
        super(message);
    }

    public SegmentAmountExceedsExpenseException(String message, Throwable cause) {
        super(message, cause);
    }
}
