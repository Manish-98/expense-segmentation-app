package com.expense.segmentation.exception;

/**
 * Base exception class for all business logic exceptions in the application. This provides a
 * common parent for all custom exceptions and allows for centralized exception handling.
 */
public abstract class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
