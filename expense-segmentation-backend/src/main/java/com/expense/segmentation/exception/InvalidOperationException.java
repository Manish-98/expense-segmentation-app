package com.expense.segmentation.exception;

/**
 * Exception thrown when a business operation is invalid or violates business rules. This typically
 * results in an HTTP 400 Bad Request response.
 */
public class InvalidOperationException extends BusinessException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
