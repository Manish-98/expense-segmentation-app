package com.expense.segmentation.exception;

/**
 * Exception thrown when segment validation fails. This typically results in an HTTP 400 Bad Request
 * response.
 */
public class SegmentValidationException extends BusinessException {

    public SegmentValidationException(String message) {
        super(message);
    }

    public SegmentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
