package com.expense.segmentation.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ExceptionTest {

    @Test
    void businessException_WithMessage_ShouldCreateException() {
        // Given
        String message = "Business logic error";

        // When
        TestBusinessException exception = new TestBusinessException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void businessException_WithMessageAndCause_ShouldCreateException() {
        // Given
        String message = "Business logic error";
        Throwable cause = new RuntimeException("Root cause");

        // When
        TestBusinessException exception = new TestBusinessException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void resourceNotFoundException_WithResourceTypeAndIdentifier_ShouldCreateException() {
        // Given
        String resourceType = "User";
        String identifier = "123";

        // When
        ResourceNotFoundException exception =
                new ResourceNotFoundException(resourceType, identifier);

        // Then
        assertThat(exception.getMessage()).contains(resourceType).contains(identifier);
    }

    @Test
    void resourceNotFoundException_WithResourceTypeFieldAndValue_ShouldCreateException() {
        // Given
        String resourceType = "User";
        String field = "email";
        String value = "test@example.com";

        // When
        ResourceNotFoundException exception =
                new ResourceNotFoundException(resourceType, field, value);

        // Then
        assertThat(exception.getMessage()).contains(resourceType).contains(field).contains(value);
    }

    @Test
    void resourceNotFoundException_WithMessage_ShouldCreateException() {
        // Given
        String message = "Resource not found";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void duplicateResourceException_WithResourceTypeFieldAndValue_ShouldCreateException() {
        // Given
        String resourceType = "User";
        String field = "email";
        String value = "test@example.com";

        // When
        DuplicateResourceException exception =
                new DuplicateResourceException(resourceType, field, value);

        // Then
        assertThat(exception.getMessage())
                .contains(resourceType)
                .contains(field)
                .contains(value)
                .contains("already exists");
    }

    @Test
    void duplicateResourceException_WithMessage_ShouldCreateException() {
        // Given
        String message = "Duplicate resource";

        // When
        DuplicateResourceException exception = new DuplicateResourceException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void invalidOperationException_WithMessage_ShouldCreateException() {
        // Given
        String message = "Invalid operation";

        // When
        InvalidOperationException exception = new InvalidOperationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void invalidOperationException_WithMessageAndCause_ShouldCreateException() {
        // Given
        String message = "Invalid operation";
        Throwable cause = new RuntimeException("Root cause");

        // When
        InvalidOperationException exception = new InvalidOperationException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void segmentValidationException_WithMessage_ShouldCreateException() {
        // Given
        String message = "Segment validation failed";

        // When
        SegmentValidationException exception = new SegmentValidationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void segmentValidationException_WithMessageAndCause_ShouldCreateException() {
        // Given
        String message = "Segment validation failed";
        Throwable cause = new RuntimeException("Root cause");

        // When
        SegmentValidationException exception = new SegmentValidationException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void segmentAmountExceedsExpenseException_WithAmounts_ShouldCreateException() {
        // Given
        BigDecimal segmentAmount = new BigDecimal("150.00");
        BigDecimal expenseAmount = new BigDecimal("100.00");

        // When
        SegmentAmountExceedsExpenseException exception =
                new SegmentAmountExceedsExpenseException(segmentAmount, expenseAmount);

        // Then
        assertThat(exception.getMessage())
                .contains("150.00")
                .contains("100.00")
                .contains("exceeds");
    }

    @Test
    void segmentAmountExceedsExpenseException_WithMessage_ShouldCreateException() {
        // Given
        String message = "Segment amount exceeds expense amount";

        // When
        SegmentAmountExceedsExpenseException exception =
                new SegmentAmountExceedsExpenseException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void segmentAmountExceedsExpenseException_WithMessageAndCause_ShouldCreateException() {
        // Given
        String message = "Segment amount exceeds expense amount";
        Throwable cause = new RuntimeException("Root cause");

        // When
        SegmentAmountExceedsExpenseException exception =
                new SegmentAmountExceedsExpenseException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    // Helper class to test abstract BusinessException
    private static class TestBusinessException extends BusinessException {
        public TestBusinessException(String message) {
            super(message);
        }

        public TestBusinessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
