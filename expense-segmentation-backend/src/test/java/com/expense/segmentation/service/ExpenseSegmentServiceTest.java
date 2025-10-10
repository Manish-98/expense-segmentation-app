package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.expense.segmentation.dto.ExpenseSegmentResponse;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.ExpenseSegmentMapper;
import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseSegment;
import com.expense.segmentation.model.ExpenseStatus;
import com.expense.segmentation.model.ExpenseType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.ExpenseSegmentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseSegmentServiceTest {

    @Mock private ExpenseSegmentRepository expenseSegmentRepository;

    @Mock private ExpenseRepository expenseRepository;

    @Mock private ExpenseSegmentMapper expenseSegmentMapper;

    @InjectMocks private ExpenseSegmentService expenseSegmentService;

    private User testUser;
    private Expense testExpense;
    private UUID expenseId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        expenseId = UUID.randomUUID();
        testExpense = new Expense();
        testExpense.setId(expenseId);
        testExpense.setDate(LocalDate.now());
        testExpense.setVendor("Test Vendor");
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setDescription("Test Description");
        testExpense.setType(ExpenseType.EXPENSE);
        testExpense.setStatus(ExpenseStatus.SUBMITTED);
        testExpense.setCreatedBy(testUser);
    }

    @Test
    void getSegmentsByExpenseId_WithExistingExpenseAndSegments_ShouldReturnMappedResponses() {
        // Arrange
        ExpenseSegment segment1 =
                createExpenseSegment("Travel", new BigDecimal("40.00"), new BigDecimal("40.00"));
        ExpenseSegment segment2 =
                createExpenseSegment("Meals", new BigDecimal("30.00"), new BigDecimal("30.00"));
        List<ExpenseSegment> segments = List.of(segment1, segment2);

        ExpenseSegmentResponse response1 =
                new ExpenseSegmentResponse(
                        UUID.randomUUID(),
                        "Travel",
                        new BigDecimal("40.00"),
                        new BigDecimal("40.00"));
        ExpenseSegmentResponse response2 =
                new ExpenseSegmentResponse(
                        UUID.randomUUID(),
                        "Meals",
                        new BigDecimal("30.00"),
                        new BigDecimal("30.00"));
        List<ExpenseSegmentResponse> expectedResponses = List.of(response1, response2);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.findByExpenseIdOrderByCategory(expenseId))
                .thenReturn(segments);
        when(expenseSegmentMapper.toResponseList(segments)).thenReturn(expectedResponses);

        // Act
        List<ExpenseSegmentResponse> actualResponses =
                expenseSegmentService.getSegmentsByExpenseId(expenseId);

        // Assert
        assertThat(actualResponses).isEqualTo(expectedResponses);
        verify(expenseRepository).findById(expenseId);
        verify(expenseSegmentRepository).findByExpenseIdOrderByCategory(expenseId);
        verify(expenseSegmentMapper).toResponseList(segments);
    }

    @Test
    void getSegmentsByExpenseId_WithExistingExpenseAndNoSegments_ShouldReturnMockData() {
        // Arrange
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.findByExpenseIdOrderByCategory(expenseId))
                .thenReturn(List.of());

        // Act
        List<ExpenseSegmentResponse> responses =
                expenseSegmentService.getSegmentsByExpenseId(expenseId);

        // Assert
        assertThat(responses).hasSize(4);

        // Verify mock data structure
        assertThat(responses)
                .extracting(ExpenseSegmentResponse::getCategory)
                .contains("Travel", "Meals", "Supplies", "Other");

        assertThat(responses)
                .extracting(ExpenseSegmentResponse::getPercentage)
                .contains(
                        new BigDecimal("40.00"),
                        new BigDecimal("30.00"),
                        new BigDecimal("20.00"),
                        new BigDecimal("10.00"));

        // Verify amounts are calculated correctly (40%, 30%, 20%, 10% of 100.00)
        assertThat(responses)
                .extracting(ExpenseSegmentResponse::getAmount)
                .contains(
                        new BigDecimal("40.0000"),
                        new BigDecimal("30.0000"),
                        new BigDecimal("20.0000"),
                        new BigDecimal("10.0000"));

        verify(expenseRepository).findById(expenseId);
        verify(expenseSegmentRepository).findByExpenseIdOrderByCategory(expenseId);
        // Mapper is not called when returning mock data
    }

    @Test
    void getSegmentsByExpenseId_WithNonExistingExpense_ShouldThrowResourceNotFoundException() {
        // Arrange
        UUID nonExistingExpenseId = UUID.randomUUID();
        when(expenseRepository.findById(nonExistingExpenseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseSegmentService.getSegmentsByExpenseId(nonExistingExpenseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Expense not found with ID: " + nonExistingExpenseId);

        verify(expenseRepository).findById(nonExistingExpenseId);
    }

    @Test
    void getSegmentsByExpenseId_WithDifferentExpenseAmount_ShouldReturnCorrectMockAmounts() {
        // Arrange
        testExpense.setAmount(new BigDecimal("200.00"));
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.findByExpenseIdOrderByCategory(expenseId))
                .thenReturn(List.of());

        // Act
        List<ExpenseSegmentResponse> responses =
                expenseSegmentService.getSegmentsByExpenseId(expenseId);

        // Assert
        assertThat(responses).hasSize(4);

        // Verify amounts are calculated correctly (40%, 30%, 20%, 10% of 200.00)
        assertThat(responses)
                .extracting(ExpenseSegmentResponse::getAmount)
                .contains(
                        new BigDecimal("80.0000"),
                        new BigDecimal("60.0000"),
                        new BigDecimal("40.0000"),
                        new BigDecimal("20.0000"));
    }

    @Test
    void getSegmentsByExpenseId_WithZeroExpenseAmount_ShouldReturnZeroMockAmounts() {
        // Arrange
        testExpense.setAmount(BigDecimal.ZERO);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.findByExpenseIdOrderByCategory(expenseId))
                .thenReturn(List.of());

        // Act
        List<ExpenseSegmentResponse> responses =
                expenseSegmentService.getSegmentsByExpenseId(expenseId);

        // Assert
        assertThat(responses).hasSize(4);

        // All amounts should be zero
        assertThat(responses)
                .allMatch(response -> response.getAmount().compareTo(BigDecimal.ZERO) == 0);

        // Percentages should still be the same
        assertThat(responses)
                .extracting(ExpenseSegmentResponse::getPercentage)
                .contains(
                        new BigDecimal("40.00"),
                        new BigDecimal("30.00"),
                        new BigDecimal("20.00"),
                        new BigDecimal("10.00"));
    }

    private ExpenseSegment createExpenseSegment(
            String category, BigDecimal amount, BigDecimal percentage) {
        ExpenseSegment segment = new ExpenseSegment();
        segment.setId(UUID.randomUUID());
        segment.setExpense(testExpense);
        segment.setCategory(category);
        segment.setAmount(amount);
        segment.setPercentage(percentage);
        return segment;
    }
}
