package com.expense.segmentation.model;

/**
 * Enum representing the status of an expense or invoice submission. This tracks the approval
 * workflow state.
 */
public enum ExpenseStatus {
    SUBMITTED,
    APPROVED,
    REJECTED,
    PENDING_REVIEW
}
