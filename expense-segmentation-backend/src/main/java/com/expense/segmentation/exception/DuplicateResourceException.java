package com.expense.segmentation.exception;

/**
 * Exception thrown when attempting to create a resource that already exists (violates unique
 * constraints). This typically results in an HTTP 409 Conflict response.
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resourceType, String field, String value) {
        super(String.format("%s with %s '%s' already exists", resourceType, field, value));
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}
