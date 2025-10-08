package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.expense.segmentation.dto.CreateExpenseRequest;
import com.expense.segmentation.dto.ExpenseResponse;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.ExpenseMapper;
import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseStatus;
import com.expense.segmentation.model.ExpenseType;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;

    @Mock private UserRepository userRepository;

    @Mock private SecurityContext securityContext;

    @Mock private Authentication authentication;

    private ExpenseService expenseService;

    private ExpenseMapper expenseMapper;

    private User testUser;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        expenseMapper = new ExpenseMapper();
        expenseService = new ExpenseService(expenseRepository, userRepository, expenseMapper);

        // Set up test role (FINANCE role has permission to view all expenses)
        Role financeRole = new Role();
        financeRole.setId(UUID.randomUUID());
        financeRole.setName(RoleType.FINANCE);
        financeRole.setDescription("Finance role");

        // Set up test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setRole(financeRole);

        // Set up test expense
        testExpense = new Expense();
        testExpense.setId(UUID.randomUUID());
        testExpense.setDate(LocalDate.now());
        testExpense.setVendor("Test Vendor");
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setDescription("Test description");
        testExpense.setType(ExpenseType.EXPENSE);
        testExpense.setCreatedBy(testUser);
        testExpense.setStatus(ExpenseStatus.SUBMITTED);

        // Mock security context (lenient since not all tests use authentication)
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(testUser.getEmail());
    }

    @Test
    void createExpense_WithValidRequest_ShouldCreateExpense() {
        // Arrange
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setDate(LocalDate.now());
        request.setVendor("Test Vendor");
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Test description");
        request.setType(ExpenseType.EXPENSE);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        // Act
        ExpenseResponse response = expenseService.createExpense(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getVendor()).isEqualTo("Test Vendor");
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(response.getType()).isEqualTo(ExpenseType.EXPENSE);

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(expenseCaptor.capture());

        Expense savedExpense = expenseCaptor.getValue();
        assertThat(savedExpense.getVendor()).isEqualTo("Test Vendor");
        assertThat(savedExpense.getCreatedBy()).isEqualTo(testUser);
        assertThat(savedExpense.getStatus()).isEqualTo(ExpenseStatus.SUBMITTED);
    }

    @Test
    void createExpense_WithNullDate_ShouldDefaultToToday() {
        // Arrange
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setDate(null); // Date is null
        request.setVendor("Test Vendor");
        request.setAmount(new BigDecimal("100.00"));
        request.setType(ExpenseType.EXPENSE);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        // Act
        expenseService.createExpense(request);

        // Assert
        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(expenseCaptor.capture());

        Expense savedExpense = expenseCaptor.getValue();
        assertThat(savedExpense.getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createExpense_WithInvalidUser_ShouldThrowException() {
        // Arrange
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setVendor("Test Vendor");
        request.setAmount(new BigDecimal("100.00"));
        request.setType(ExpenseType.EXPENSE);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.createExpense(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void getAllExpenses_ShouldReturnAllExpenses() {
        // Arrange
        Expense expense2 = new Expense();
        expense2.setId(UUID.randomUUID());
        expense2.setVendor("Vendor 2");
        expense2.setAmount(new BigDecimal("200.00"));
        expense2.setType(ExpenseType.INVOICE);
        expense2.setCreatedBy(testUser);

        when(expenseRepository.findAllWithCreatedBy())
                .thenReturn(Arrays.asList(testExpense, expense2));

        // Act
        List<ExpenseResponse> responses = expenseService.getAllExpenses();

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getVendor()).isEqualTo("Test Vendor");
        assertThat(responses.get(1).getVendor()).isEqualTo("Vendor 2");
    }

    @Test
    void getExpenseById_WithValidId_ShouldReturnExpense() {
        // Arrange
        UUID expenseId = testExpense.getId();
        when(expenseRepository.findByIdWithCreatedBy(expenseId))
                .thenReturn(Optional.of(testExpense));

        // Act
        ExpenseResponse response = expenseService.getExpenseById(expenseId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(expenseId);
        assertThat(response.getVendor()).isEqualTo("Test Vendor");
    }

    @Test
    void getExpenseById_WithInvalidId_ShouldThrowException() {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        when(expenseRepository.findByIdWithCreatedBy(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.getExpenseById(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense");
    }

    @Test
    void getExpensesByUser_WithValidUserId_ShouldReturnUserExpenses() {
        // Arrange
        UUID userId = testUser.getId();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByCreatedById(userId)).thenReturn(Arrays.asList(testExpense));

        // Act
        List<ExpenseResponse> responses = expenseService.getExpensesByUser(userId);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCreatedById()).isEqualTo(testUser.getId());
    }

    @Test
    void getExpensesByUser_WithInvalidUserId_ShouldThrowException() {
        // Arrange
        UUID invalidUserId = UUID.randomUUID();
        when(userRepository.existsById(invalidUserId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> expenseService.getExpensesByUser(invalidUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }
}
