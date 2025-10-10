package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.expense.segmentation.dto.CreateExpenseSegmentRequest;
import com.expense.segmentation.dto.CreateMultipleExpenseSegmentsRequest;
import com.expense.segmentation.dto.ExpenseSegmentResponse;
import com.expense.segmentation.exception.SegmentValidationException;
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
    }

    @Test
    void addExpenseSegment_WithExistingSegments_ShouldThrowException() {
        // Arrange
        CreateExpenseSegmentRequest request = new CreateExpenseSegmentRequest();
        request.setCategory("Travel");
        request.setAmount(new BigDecimal("50.00"));

        ExpenseSegment existingSegment =
                createExpenseSegment("Meals", new BigDecimal("30.00"), new BigDecimal("30.00"));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.findByExpenseIdOrderByCategory(expenseId))
                .thenReturn(List.of(existingSegment));

        // Act & Assert
        assertThatThrownBy(() -> expenseSegmentService.addExpenseSegment(expenseId, request))
                .isInstanceOf(SegmentValidationException.class)
                .hasMessageContaining("Expense already has segments");

        verify(expenseRepository).findById(expenseId);
        verify(expenseSegmentRepository).findByExpenseIdOrderByCategory(expenseId);
    }

    @Test
    void addExpenseSegment_WithPercentageProvided_ShouldUseProvidedPercentage() {
        // Arrange
        CreateExpenseSegmentRequest request = new CreateExpenseSegmentRequest();
        request.setCategory("Travel");
        request.setAmount(new BigDecimal("30.00"));
        request.setPercentage(new BigDecimal("35.00")); // Custom percentage

        ExpenseSegment savedSegment =
                createExpenseSegment("Travel", new BigDecimal("30.00"), new BigDecimal("35.00"));
        ExpenseSegmentResponse expectedResponse =
                new ExpenseSegmentResponse(
                        savedSegment.getId(),
                        "Travel",
                        new BigDecimal("30.00"),
                        new BigDecimal("35.00"));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.findByExpenseIdOrderByCategory(expenseId))
                .thenReturn(List.of());
        when(expenseSegmentRepository.save(any(ExpenseSegment.class))).thenReturn(savedSegment);
        when(expenseSegmentMapper.toResponseList(List.of(savedSegment)))
                .thenReturn(List.of(expectedResponse));

        // Act
        List<ExpenseSegmentResponse> responses =
                expenseSegmentService.addExpenseSegment(expenseId, request);

        // Assert
        assertThat(responses.get(0).getPercentage()).isEqualTo(new BigDecimal("35.00"));
    }

    @Test
    void addMultipleExpenseSegments_WithValidData_ShouldCreateAllSegments() {
        // Arrange
        CreateExpenseSegmentRequest segment1 = new CreateExpenseSegmentRequest();
        segment1.setCategory("Travel");
        segment1.setAmount(new BigDecimal("40.00"));

        CreateExpenseSegmentRequest segment2 = new CreateExpenseSegmentRequest();
        segment2.setCategory("Meals");
        segment2.setAmount(new BigDecimal("60.00"));

        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment1, segment2));

        ExpenseSegment savedSegment1 =
                createExpenseSegment("Travel", new BigDecimal("40.00"), new BigDecimal("40.00"));
        ExpenseSegment savedSegment2 =
                createExpenseSegment("Meals", new BigDecimal("60.00"), new BigDecimal("60.00"));

        ExpenseSegmentResponse response1 =
                new ExpenseSegmentResponse(
                        savedSegment1.getId(),
                        "Travel",
                        new BigDecimal("40.00"),
                        new BigDecimal("40.00"));
        ExpenseSegmentResponse response2 =
                new ExpenseSegmentResponse(
                        savedSegment2.getId(),
                        "Meals",
                        new BigDecimal("60.00"),
                        new BigDecimal("60.00"));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.saveAll(any()))
                .thenReturn(List.of(savedSegment1, savedSegment2));
        when(expenseSegmentMapper.toResponseList(List.of(savedSegment1, savedSegment2)))
                .thenReturn(List.of(response1, response2));

        // Act
        List<ExpenseSegmentResponse> responses =
                expenseSegmentService.addMultipleExpenseSegments(expenseId, request);

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("category").containsExactly("Travel", "Meals");
        assertThat(responses)
                .extracting("amount")
                .containsExactly(new BigDecimal("40.00"), new BigDecimal("60.00"));

        verify(expenseRepository).findById(expenseId);
        verify(expenseSegmentRepository).deleteByExpenseId(expenseId);
        verify(expenseSegmentRepository).saveAll(any());
    }

    @Test
    void addMultipleExpenseSegments_WithIncorrectTotal_ShouldThrowException() {
        // Arrange
        CreateExpenseSegmentRequest segment1 = new CreateExpenseSegmentRequest();
        segment1.setCategory("Travel");
        segment1.setAmount(new BigDecimal("40.00"));

        CreateExpenseSegmentRequest segment2 = new CreateExpenseSegmentRequest();
        segment2.setCategory("Meals");
        segment2.setAmount(new BigDecimal("70.00")); // Total 110, but expense is 100

        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment1, segment2));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));

        // Act & Assert
        assertThatThrownBy(
                        () -> expenseSegmentService.addMultipleExpenseSegments(expenseId, request))
                .isInstanceOf(SegmentValidationException.class)
                .hasMessageContaining(
                        "Total segments amount (110.00) must equal expense amount (100.00)");

        verify(expenseRepository).findById(expenseId);
    }

    @Test
    void addMultipleExpenseSegments_WithDuplicateCategories_ShouldThrowException() {
        // Arrange
        CreateExpenseSegmentRequest segment1 = new CreateExpenseSegmentRequest();
        segment1.setCategory("Travel");
        segment1.setAmount(new BigDecimal("40.00"));

        CreateExpenseSegmentRequest segment2 = new CreateExpenseSegmentRequest();
        segment2.setCategory("Travel"); // Duplicate category
        segment2.setAmount(new BigDecimal("60.00"));

        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment1, segment2));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));

        // Act & Assert
        assertThatThrownBy(
                        () -> expenseSegmentService.addMultipleExpenseSegments(expenseId, request))
                .isInstanceOf(SegmentValidationException.class)
                .hasMessageContaining("Segment categories must be unique");

        verify(expenseRepository).findById(expenseId);
    }

    @Test
    void addMultipleExpenseSegments_WithRoundingDifference_ShouldAllowSmallDifference() {
        // Arrange
        CreateExpenseSegmentRequest segment1 = new CreateExpenseSegmentRequest();
        segment1.setCategory("Travel");
        segment1.setAmount(new BigDecimal("33.33"));

        CreateExpenseSegmentRequest segment2 = new CreateExpenseSegmentRequest();
        segment2.setCategory("Meals");
        segment2.setAmount(new BigDecimal("33.34"));

        CreateExpenseSegmentRequest segment3 = new CreateExpenseSegmentRequest();
        segment3.setCategory("Supplies");
        segment3.setAmount(
                new BigDecimal(
                        "33.33")); // Total 100.00, but due to rounding might be 99.99 or 100.01

        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment1, segment2, segment3));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.saveAll(any())).thenReturn(List.of());
        when(expenseSegmentMapper.toResponseList(any())).thenReturn(List.of());

        // Act & Should not throw exception
        List<ExpenseSegmentResponse> responses =
                expenseSegmentService.addMultipleExpenseSegments(expenseId, request);

        verify(expenseRepository).findById(expenseId);
        verify(expenseSegmentRepository).deleteByExpenseId(expenseId);
        verify(expenseSegmentRepository).saveAll(any());
    }

    @Test
    void addMultipleExpenseSegments_WithValidData_ShouldCreateAndReturnSegments() {
        // Arrange
        CreateExpenseSegmentRequest segment1 = new CreateExpenseSegmentRequest();
        segment1.setCategory("Travel");
        segment1.setAmount(new BigDecimal("40.00"));

        CreateExpenseSegmentRequest segment2 = new CreateExpenseSegmentRequest();
        segment2.setCategory("Meals");
        segment2.setAmount(new BigDecimal("30.00"));

        CreateExpenseSegmentRequest segment3 = new CreateExpenseSegmentRequest();
        segment3.setCategory("Supplies");
        segment3.setAmount(new BigDecimal("30.00"));

        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment1, segment2, segment3));

        ExpenseSegment savedSegment1 =
                createExpenseSegment("Travel", new BigDecimal("40.00"), new BigDecimal("40.00"));
        ExpenseSegment savedSegment2 =
                createExpenseSegment("Meals", new BigDecimal("30.00"), new BigDecimal("30.00"));
        ExpenseSegment savedSegment3 =
                createExpenseSegment("Supplies", new BigDecimal("30.00"), new BigDecimal("30.00"));
        List<ExpenseSegment> savedSegments = List.of(savedSegment1, savedSegment2, savedSegment3);

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
        ExpenseSegmentResponse response3 =
                new ExpenseSegmentResponse(
                        UUID.randomUUID(),
                        "Supplies",
                        new BigDecimal("30.00"),
                        new BigDecimal("30.00"));
        List<ExpenseSegmentResponse> expectedResponses = List.of(response1, response2, response3);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.saveAll(any())).thenReturn(savedSegments);
        when(expenseSegmentMapper.toResponseList(savedSegments)).thenReturn(expectedResponses);

        // Act
        List<ExpenseSegmentResponse> actualResponses =
                expenseSegmentService.addMultipleExpenseSegments(expenseId, request);

        // Assert
        assertThat(actualResponses).isEqualTo(expectedResponses);
        verify(expenseRepository).findById(expenseId);
        verify(expenseSegmentRepository).deleteByExpenseId(expenseId);
        verify(expenseSegmentRepository).saveAll(any());
    }

    @Test
    void addMultipleExpenseSegments_WithTotalAmountMismatch_ShouldThrowValidationException() {
        // Arrange
        CreateExpenseSegmentRequest segment1 = new CreateExpenseSegmentRequest();
        segment1.setCategory("Travel");
        segment1.setAmount(new BigDecimal("40.00"));

        CreateExpenseSegmentRequest segment2 = new CreateExpenseSegmentRequest();
        segment2.setCategory("Meals");
        segment2.setAmount(new BigDecimal("30.00"));

        // Total is 70.00, but expense is 100.00
        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment1, segment2));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));

        // Act & Assert
        assertThatThrownBy(
                        () -> expenseSegmentService.addMultipleExpenseSegments(expenseId, request))
                .isInstanceOf(SegmentValidationException.class)
                .hasMessageContaining(
                        "Total segments amount (70.00) must equal expense amount (100.00)");
    }

    @Test
    void addMultipleExpenseSegments_WithDuplicateCategories_ShouldThrowValidationException() {
        // Arrange
        CreateExpenseSegmentRequest segment1 = new CreateExpenseSegmentRequest();
        segment1.setCategory("Travel");
        segment1.setAmount(new BigDecimal("40.00"));

        CreateExpenseSegmentRequest segment2 = new CreateExpenseSegmentRequest();
        segment2.setCategory("Travel"); // Duplicate category
        segment2.setAmount(new BigDecimal("60.00"));

        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment1, segment2));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));

        // Act & Assert
        assertThatThrownBy(
                        () -> expenseSegmentService.addMultipleExpenseSegments(expenseId, request))
                .isInstanceOf(SegmentValidationException.class)
                .hasMessage("Segment categories must be unique within an expense");
    }

    @Test
    void addMultipleExpenseSegments_WithProvidedPercentages_ShouldUseProvidedValues() {
        // Arrange
        CreateExpenseSegmentRequest segment1 = new CreateExpenseSegmentRequest();
        segment1.setCategory("Travel");
        segment1.setAmount(new BigDecimal("50.00"));
        segment1.setPercentage(new BigDecimal("50.00")); // Explicit percentage

        CreateExpenseSegmentRequest segment2 = new CreateExpenseSegmentRequest();
        segment2.setCategory("Meals");
        segment2.setAmount(new BigDecimal("50.00"));
        segment2.setPercentage(new BigDecimal("50.00")); // Explicit percentage

        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment1, segment2));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.saveAll(any())).thenReturn(List.of());
        when(expenseSegmentMapper.toResponseList(any())).thenReturn(List.of());

        // Act
        List<ExpenseSegmentResponse> responses =
                expenseSegmentService.addMultipleExpenseSegments(expenseId, request);

        // Assert
        verify(expenseRepository).findById(expenseId);
        verify(expenseSegmentRepository).deleteByExpenseId(expenseId);
        verify(expenseSegmentRepository).saveAll(any());
    }

    @Test
    void addMultipleExpenseSegments_WithNullPercentages_ShouldAutoCalculate() {
        // Arrange
        CreateExpenseSegmentRequest segment1 = new CreateExpenseSegmentRequest();
        segment1.setCategory("Travel");
        segment1.setAmount(new BigDecimal("25.00"));
        segment1.setPercentage(null); // Should be auto-calculated to 25.00%

        CreateExpenseSegmentRequest segment2 = new CreateExpenseSegmentRequest();
        segment2.setCategory("Meals");
        segment2.setAmount(new BigDecimal("75.00"));
        segment2.setPercentage(null); // Should be auto-calculated to 75.00%

        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment1, segment2));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.saveAll(any())).thenReturn(List.of());
        when(expenseSegmentMapper.toResponseList(any())).thenReturn(List.of());

        // Act
        List<ExpenseSegmentResponse> responses =
                expenseSegmentService.addMultipleExpenseSegments(expenseId, request);

        // Assert
        verify(expenseRepository).findById(expenseId);
        verify(expenseSegmentRepository).deleteByExpenseId(expenseId);
        verify(expenseSegmentRepository).saveAll(any());
    }

    @Test
    void addMultipleExpenseSegments_WithZeroExpenseAmount_ShouldCalculateZeroPercentages() {
        // Arrange
        testExpense.setAmount(BigDecimal.ZERO);

        CreateExpenseSegmentRequest segment1 = new CreateExpenseSegmentRequest();
        segment1.setCategory("Travel");
        segment1.setAmount(BigDecimal.ZERO);
        segment1.setPercentage(null);

        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment1));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.saveAll(any())).thenReturn(List.of());
        when(expenseSegmentMapper.toResponseList(any())).thenReturn(List.of());

        // Act
        List<ExpenseSegmentResponse> responses =
                expenseSegmentService.addMultipleExpenseSegments(expenseId, request);

        // Assert
        verify(expenseRepository).findById(expenseId);
        verify(expenseSegmentRepository).deleteByExpenseId(expenseId);
        verify(expenseSegmentRepository).saveAll(any());
    }

    @Test
    void calculatePercentage_WithValidInputs_ShouldReturnCorrectPercentage() {
        // This test verifies the private method behavior through public API
        // Arrange
        CreateExpenseSegmentRequest segment = new CreateExpenseSegmentRequest();
        segment.setCategory("Travel");
        segment.setAmount(new BigDecimal("100.00")); // Full amount to match expense
        segment.setPercentage(null);

        CreateMultipleExpenseSegmentsRequest request = new CreateMultipleExpenseSegmentsRequest();
        request.setSegments(List.of(segment));

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(expenseSegmentRepository.saveAll(any())).thenReturn(List.of());
        when(expenseSegmentMapper.toResponseList(any())).thenReturn(List.of());

        // Act
        List<ExpenseSegmentResponse> responses =
                expenseSegmentService.addMultipleExpenseSegments(expenseId, request);

        // Assert - 100.00 out of 100.00 should be 100.00%
        verify(expenseRepository).findById(expenseId);
        verify(expenseSegmentRepository).saveAll(any());
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
