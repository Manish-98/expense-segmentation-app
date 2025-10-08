package com.expense.segmentation.exception;

/**
 * Exception thrown when a requested resource (entity) is not found in the database. This typically
 * results in an HTTP 404 Not Found response.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s not found with id: %s", resourceType, identifier));
    }

    public ResourceNotFoundException(String resourceType, String field, String value) {
        super(String.format("%s not found with %s: %s", resourceType, field, value));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
