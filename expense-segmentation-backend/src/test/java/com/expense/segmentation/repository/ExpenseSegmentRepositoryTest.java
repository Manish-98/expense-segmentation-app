package com.expense.segmentation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseSegment;
import com.expense.segmentation.model.ExpenseStatus;
import com.expense.segmentation.model.ExpenseType;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ExpenseSegmentRepositoryTest {

    @Autowired private TestEntityManager entityManager;

    @Autowired private ExpenseSegmentRepository expenseSegmentRepository;

    private User testUser;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        // Create and save role
        Role employeeRole = new Role();
        employeeRole.setName(RoleType.EMPLOYEE);
        entityManager.persist(employeeRole);

        // Create and save user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("password");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRole(employeeRole);
        entityManager.persist(testUser);

        testExpense = new Expense();
        testExpense.setDate(LocalDate.now());
        testExpense.setVendor("Test Vendor");
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setDescription("Test Description");
        testExpense.setType(ExpenseType.EXPENSE);
        testExpense.setStatus(ExpenseStatus.SUBMITTED);
        testExpense.setCreatedBy(testUser);
        entityManager.persist(testExpense);
        entityManager.flush();
    }

    @Test
    void findByExpenseId_WithExistingExpenseId_ShouldReturnSegments() {
        // Arrange
        ExpenseSegment segment1 =
                createExpenseSegment("Travel", new BigDecimal("40.00"), new BigDecimal("40.00"));
        ExpenseSegment segment2 =
                createExpenseSegment("Meals", new BigDecimal("30.00"), new BigDecimal("30.00"));

        entityManager.persist(segment1);
        entityManager.persist(segment2);
        entityManager.flush();

        // Act
        List<ExpenseSegment> segments =
                expenseSegmentRepository.findByExpenseId(testExpense.getId());

        // Assert
        assertThat(segments).hasSize(2);
        assertThat(segments)
                .extracting(ExpenseSegment::getCategory)
                .containsExactlyInAnyOrder("Travel", "Meals");
    }

    @Test
    void findByExpenseId_WithNonExistingExpenseId_ShouldReturnEmpty() {
        // Act
        List<ExpenseSegment> segments = expenseSegmentRepository.findByExpenseId(UUID.randomUUID());

        // Assert
        assertThat(segments).isEmpty();
    }

    @Test
    void findByExpenseIdOrderByCategory_WithExistingExpenseId_ShouldReturnOrderedSegments() {
        // Arrange
        ExpenseSegment segment1 =
                createExpenseSegment("Travel", new BigDecimal("40.00"), new BigDecimal("40.00"));
        ExpenseSegment segment2 =
                createExpenseSegment("Meals", new BigDecimal("30.00"), new BigDecimal("30.00"));
        ExpenseSegment segment3 =
                createExpenseSegment("Supplies", new BigDecimal("20.00"), new BigDecimal("20.00"));

        entityManager.persist(segment2); // Meals
        entityManager.persist(segment1); // Travel
        entityManager.persist(segment3); // Supplies
        entityManager.flush();

        // Act
        List<ExpenseSegment> segments =
                expenseSegmentRepository.findByExpenseIdOrderByCategory(testExpense.getId());

        // Assert
        assertThat(segments).hasSize(3);
        assertThat(segments)
                .extracting(ExpenseSegment::getCategory)
                .containsExactly("Meals", "Supplies", "Travel");
    }

    @Test
    void deleteByExpenseId_WithExistingExpenseId_ShouldDeleteAllSegments() {
        // Arrange
        ExpenseSegment segment1 =
                createExpenseSegment("Travel", new BigDecimal("40.00"), new BigDecimal("40.00"));
        ExpenseSegment segment2 =
                createExpenseSegment("Meals", new BigDecimal("30.00"), new BigDecimal("30.00"));

        entityManager.persist(segment1);
        entityManager.persist(segment2);
        entityManager.flush();

        // Verify segments exist
        List<ExpenseSegment> beforeDelete =
                expenseSegmentRepository.findByExpenseId(testExpense.getId());
        assertThat(beforeDelete).hasSize(2);

        // Act
        expenseSegmentRepository.deleteByExpenseId(testExpense.getId());
        entityManager.flush();

        // Assert
        List<ExpenseSegment> afterDelete =
                expenseSegmentRepository.findByExpenseId(testExpense.getId());
        assertThat(afterDelete).isEmpty();
    }

    @Test
    void save_ShouldPersistExpenseSegment() {
        // Arrange
        ExpenseSegment segment =
                createExpenseSegment("Travel", new BigDecimal("40.00"), new BigDecimal("40.00"));

        // Act
        ExpenseSegment savedSegment = expenseSegmentRepository.save(segment);
        entityManager.flush();

        // Assert
        ExpenseSegment foundSegment =
                entityManager.find(ExpenseSegment.class, savedSegment.getId());
        assertThat(foundSegment).isNotNull();
        assertThat(foundSegment.getCategory()).isEqualTo("Travel");
        assertThat(foundSegment.getAmount()).isEqualTo(new BigDecimal("40.00"));
        assertThat(foundSegment.getPercentage()).isEqualTo(new BigDecimal("40.00"));
        assertThat(foundSegment.getExpense().getId()).isEqualTo(testExpense.getId());
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
