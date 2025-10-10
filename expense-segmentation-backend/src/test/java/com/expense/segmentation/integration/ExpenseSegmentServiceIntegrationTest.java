package com.expense.segmentation.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseSegment;
import com.expense.segmentation.model.ExpenseStatus;
import com.expense.segmentation.model.ExpenseType;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.ExpenseSegmentRepository;
import com.expense.segmentation.repository.RoleRepository;
import com.expense.segmentation.repository.UserRepository;
import com.expense.segmentation.service.ExpenseSegmentService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExpenseSegmentServiceIntegrationTest {

    @Autowired private ExpenseSegmentService expenseSegmentService;

    @Autowired private ExpenseRepository expenseRepository;

    @Autowired private ExpenseSegmentRepository expenseSegmentRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private RoleRepository roleRepository;

    private User testUser;
    private Expense testExpense;
    private UUID expenseId;

    @BeforeEach
    void setUp() {
        // Clean up segment data to avoid interference
        expenseSegmentRepository.deleteAll();
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        // Check if role already exists, if not create it
        Role employeeRole = roleRepository.findByName(RoleType.EMPLOYEE).orElse(null);
        if (employeeRole == null) {
            employeeRole = new Role();
            employeeRole.setName(RoleType.EMPLOYEE);
            employeeRole.setDescription("Employee role");
            employeeRole = roleRepository.save(employeeRole);
        }

        // Create and save user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test-" + UUID.randomUUID() + "@example.com");
        testUser.setPasswordHash("password");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRole(employeeRole);
        testUser = userRepository.save(testUser);

        // Create and save expense
        testExpense = new Expense();
        testExpense.setDate(LocalDate.now());
        testExpense.setVendor("Test Vendor");
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setDescription("Test Description");
        testExpense.setType(ExpenseType.EXPENSE);
        testExpense.setStatus(ExpenseStatus.SUBMITTED);
        testExpense.setCreatedBy(testUser);
        testExpense = expenseRepository.save(testExpense);
        expenseId = testExpense.getId();
    }

    @Test
    void getSegmentsByExpenseId_WithExistingExpenseAndNoSegments_ShouldReturnEmptyList() {
        // Act
        List<com.expense.segmentation.dto.ExpenseSegmentResponse> responses =
                expenseSegmentService.getSegmentsByExpenseId(expenseId);

        // Assert
        assertThat(responses).isEmpty();
    }

    @Test
    void getSegmentsByExpenseId_WithExistingExpenseAndSegments_ShouldReturnStoredSegments() {
        // Arrange
        ExpenseSegment segment1 =
                createExpenseSegment("Travel", new BigDecimal("50.00"), new BigDecimal("50.00"));
        ExpenseSegment segment2 =
                createExpenseSegment("Meals", new BigDecimal("25.00"), new BigDecimal("25.00"));
        ExpenseSegment segment3 =
                createExpenseSegment("Supplies", new BigDecimal("25.00"), new BigDecimal("25.00"));

        expenseSegmentRepository.save(segment1);
        expenseSegmentRepository.save(segment2);
        expenseSegmentRepository.save(segment3);
        expenseSegmentRepository.flush();

        // Act
        List<com.expense.segmentation.dto.ExpenseSegmentResponse> responses =
                expenseSegmentService.getSegmentsByExpenseId(expenseId);

        // Assert
        assertThat(responses).hasSize(3);
        assertThat(responses)
                .extracting("category")
                .containsExactlyInAnyOrder("Travel", "Meals", "Supplies");
        assertThat(responses)
                .extracting("amount")
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(
                        new BigDecimal("50.00"), new BigDecimal("25.00"), new BigDecimal("25.00"));
        assertThat(responses)
                .extracting("percentage")
                .containsExactlyInAnyOrder(
                        new BigDecimal("50.00"), new BigDecimal("25.00"), new BigDecimal("25.00"));
    }

    @Test
    void getSegmentsByExpenseId_WithNonExistingExpense_ShouldThrowException() {
        // Arrange
        UUID nonExistingExpenseId = UUID.randomUUID();

        // Act & Assert
        assertThatThrownBy(() -> expenseSegmentService.getSegmentsByExpenseId(nonExistingExpenseId))
                .isInstanceOf(com.expense.segmentation.exception.ResourceNotFoundException.class)
                .hasMessage("Expense not found with ID: " + nonExistingExpenseId);
    }

    @Test
    void getSegmentsByExpenseId_WithDifferentExpenseAmounts_ShouldReturnEmptyList() {
        // Arrange
        testExpense.setAmount(new BigDecimal("200.00"));
        expenseRepository.save(testExpense);

        // Act
        List<com.expense.segmentation.dto.ExpenseSegmentResponse> responses =
                expenseSegmentService.getSegmentsByExpenseId(expenseId);

        // Assert
        assertThat(responses).isEmpty();
    }

    @Test
    void getSegmentsByExpenseId_WithZeroExpenseAmount_ShouldReturnEmptyList() {
        // Arrange
        testExpense.setAmount(BigDecimal.ZERO);
        expenseRepository.save(testExpense);

        // Act
        List<com.expense.segmentation.dto.ExpenseSegmentResponse> responses =
                expenseSegmentService.getSegmentsByExpenseId(expenseId);

        // Assert
        assertThat(responses).isEmpty();
    }

    @Test
    void getSegmentsByExpenseId_WithMultipleExpenses_ShouldReturnIndependentSegments() {
        // Arrange
        Expense expense2 = new Expense();
        expense2.setDate(LocalDate.now());
        expense2.setVendor("Test Vendor 2");
        expense2.setAmount(new BigDecimal("300.00"));
        expense2.setDescription("Test Description 2");
        expense2.setType(ExpenseType.EXPENSE);
        expense2.setStatus(ExpenseStatus.SUBMITTED);
        expense2.setCreatedBy(testUser);
        expense2 = expenseRepository.save(expense2);

        // Add segments to first expense
        ExpenseSegment segment1 =
                createExpenseSegment("Travel", new BigDecimal("60.00"), new BigDecimal("60.00"));
        expenseSegmentRepository.save(segment1);
        expenseSegmentRepository.flush();

        // Act
        List<com.expense.segmentation.dto.ExpenseSegmentResponse> responses1 =
                expenseSegmentService.getSegmentsByExpenseId(expenseId);
        List<com.expense.segmentation.dto.ExpenseSegmentResponse> responses2 =
                expenseSegmentService.getSegmentsByExpenseId(expense2.getId());

        // Assert
        assertThat(responses1).hasSize(1);
        assertThat(responses1.get(0).getCategory()).isEqualTo("Travel");
        assertThat(responses1.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("60.00"));

        assertThat(responses2).isEmpty(); // No segments for expense with no segments
    }

    @Test
    void getSegmentsByExpenseId_WithSegmentsOrderedByCategory_ShouldReturnOrderedResponses() {
        // Arrange
        ExpenseSegment segment1 =
                createExpenseSegment("Travel", new BigDecimal("40.00"), new BigDecimal("40.00"));
        ExpenseSegment segment2 =
                createExpenseSegment("Meals", new BigDecimal("30.00"), new BigDecimal("30.00"));
        ExpenseSegment segment3 =
                createExpenseSegment("Supplies", new BigDecimal("20.00"), new BigDecimal("20.00"));
        ExpenseSegment segment4 =
                createExpenseSegment(
                        "Accommodation", new BigDecimal("10.00"), new BigDecimal("10.00"));

        expenseSegmentRepository.save(segment3);
        expenseSegmentRepository.save(segment1);
        expenseSegmentRepository.save(segment4);
        expenseSegmentRepository.save(segment2);
        expenseSegmentRepository.flush(); // Random order

        // Act
        List<com.expense.segmentation.dto.ExpenseSegmentResponse> responses =
                expenseSegmentService.getSegmentsByExpenseId(expenseId);

        // Assert
        assertThat(responses).hasSize(4);
        assertThat(responses)
                .extracting("category")
                .containsExactly(
                        "Accommodation", "Meals", "Supplies", "Travel"); // Alphabetical order
    }

    private ExpenseSegment createExpenseSegment(
            String category, BigDecimal amount, BigDecimal percentage) {
        ExpenseSegment segment = new ExpenseSegment();
        segment.setExpense(testExpense);
        segment.setCategory(category);
        segment.setAmount(amount);
        segment.setPercentage(percentage);
        return segment;
    }
}
