package com.expense.segmentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error response DTO for all API errors. Provides consistent error format across the
 * application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** HTTP status code */
    private int status;

    /** Error type/category */
    private String error;

    /** Human-readable error message */
    private String message;

    /** API path where the error occurred */
    private String path;

    /** Timestamp when the error occurred */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /** List of validation errors (for validation failures) */
    private List<ValidationError> validationErrors;

    /**
     * Nested class for field-level validation errors
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
