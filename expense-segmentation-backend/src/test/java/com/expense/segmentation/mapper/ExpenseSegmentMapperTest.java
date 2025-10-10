package com.expense.segmentation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.expense.segmentation.dto.ExpenseSegmentResponse;
import com.expense.segmentation.model.ExpenseSegment;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpenseSegmentMapperTest {

    private ExpenseSegmentMapper expenseSegmentMapper;
    private ExpenseSegment testExpenseSegment;

    @BeforeEach
    void setUp() {
        expenseSegmentMapper = new ExpenseSegmentMapper();

        testExpenseSegment = new ExpenseSegment();
        testExpenseSegment.setId(UUID.randomUUID());
        testExpenseSegment.setCategory("Travel");
        testExpenseSegment.setAmount(new BigDecimal("40.00"));
        testExpenseSegment.setPercentage(new BigDecimal("40.00"));
    }

    @Test
    void toResponse_WithValidExpenseSegment_ShouldReturnExpenseSegmentResponse() {
        // Act
        ExpenseSegmentResponse response = expenseSegmentMapper.toResponse(testExpenseSegment);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testExpenseSegment.getId());
        assertThat(response.getCategory()).isEqualTo("Travel");
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("40.00"));
        assertThat(response.getPercentage()).isEqualTo(new BigDecimal("40.00"));
    }

    @Test
    void toResponse_WithNullExpenseSegment_ShouldReturnNull() {
        // Act
        ExpenseSegmentResponse response = expenseSegmentMapper.toResponse(null);

        // Assert
        assertThat(response).isNull();
    }

    @Test
    void toResponse_WithZeroAmountAndPercentage_ShouldReturnCorrectValues() {
        // Arrange
        testExpenseSegment.setAmount(BigDecimal.ZERO);
        testExpenseSegment.setPercentage(BigDecimal.ZERO);

        // Act
        ExpenseSegmentResponse response = expenseSegmentMapper.toResponse(testExpenseSegment);

        // Assert
        assertThat(response.getAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getPercentage()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void toResponseList_WithValidList_ShouldReturnListOfResponses() {
        // Arrange
        ExpenseSegment segment1 =
                createExpenseSegment("Travel", new BigDecimal("40.00"), new BigDecimal("40.00"));
        ExpenseSegment segment2 =
                createExpenseSegment("Meals", new BigDecimal("30.00"), new BigDecimal("30.00"));
        List<ExpenseSegment> segments = List.of(segment1, segment2);

        // Act
        List<ExpenseSegmentResponse> responses = expenseSegmentMapper.toResponseList(segments);

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCategory()).isEqualTo("Travel");
        assertThat(responses.get(0).getAmount()).isEqualTo(new BigDecimal("40.00"));
        assertThat(responses.get(0).getPercentage()).isEqualTo(new BigDecimal("40.00"));
        assertThat(responses.get(1).getCategory()).isEqualTo("Meals");
        assertThat(responses.get(1).getAmount()).isEqualTo(new BigDecimal("30.00"));
        assertThat(responses.get(1).getPercentage()).isEqualTo(new BigDecimal("30.00"));
    }

    @Test
    void toResponseList_WithEmptyList_ShouldReturnEmptyList() {
        // Arrange
        List<ExpenseSegment> segments = List.of();

        // Act
        List<ExpenseSegmentResponse> responses = expenseSegmentMapper.toResponseList(segments);

        // Assert
        assertThat(responses).isEmpty();
    }

    @Test
    void toResponseList_WithNullList_ShouldReturnEmptyList() {
        // Act
        List<ExpenseSegmentResponse> responses = expenseSegmentMapper.toResponseList(null);

        // Assert
        assertThat(responses).isEmpty();
    }

    @Test
    void toResponseList_WithSingleElement_ShouldReturnSingleResponse() {
        // Arrange
        List<ExpenseSegment> segments = List.of(testExpenseSegment);

        // Act
        List<ExpenseSegmentResponse> responses = expenseSegmentMapper.toResponseList(segments);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(testExpenseSegment.getId());
        assertThat(responses.get(0).getCategory()).isEqualTo("Travel");
    }

    private ExpenseSegment createExpenseSegment(
            String category, BigDecimal amount, BigDecimal percentage) {
        ExpenseSegment segment = new ExpenseSegment();
        segment.setId(UUID.randomUUID());
        segment.setCategory(category);
        segment.setAmount(amount);
        segment.setPercentage(percentage);
        return segment;
    }
}
