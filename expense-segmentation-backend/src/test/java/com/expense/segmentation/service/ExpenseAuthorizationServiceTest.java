package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseStatus;
import com.expense.segmentation.model.ExpenseType;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseAuthorizationServiceTest {

    @Mock private ExpenseRepository expenseRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks private ExpenseAuthorizationService expenseAuthorizationService;

    private User testUser;
    private User adminUser;
    private User financeUser;
    private User managerUser;
    private User otherUser;
    private Expense testExpense;
    private UUID expenseId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        // Create roles
        Role userRole = new Role();
        userRole.setName(RoleType.EMPLOYEE);

        Role adminRole = new Role();
        adminRole.setName(RoleType.ADMIN);

        Role financeRole = new Role();
        financeRole.setName(RoleType.FINANCE);

        Role managerRole = new Role();
        managerRole.setName(RoleType.MANAGER);

        // Create users
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(userRole);
        testUser.setStatus(UserStatus.ACTIVE);

        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(adminRole);
        adminUser.setStatus(UserStatus.ACTIVE);

        financeUser = new User();
        financeUser.setId(UUID.randomUUID());
        financeUser.setName("Finance User");
        financeUser.setEmail("finance@example.com");
        financeUser.setRole(financeRole);
        financeUser.setStatus(UserStatus.ACTIVE);

        managerUser = new User();
        managerUser.setId(UUID.randomUUID());
        managerUser.setName("Manager User");
        managerUser.setEmail("manager@example.com");
        managerUser.setRole(managerRole);
        managerUser.setStatus(UserStatus.ACTIVE);

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setName("Other User");
        otherUser.setEmail("other@example.com");
        otherUser.setRole(userRole);
        otherUser.setStatus(UserStatus.ACTIVE);

        // Create expense
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
    void canModifyExpense_WithOwner_ShouldReturnTrue() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        boolean result =
                expenseAuthorizationService.canModifyExpense(expenseId, "test@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canModifyExpense_WithAdminRole_ShouldReturnTrue() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // When
        boolean result =
                expenseAuthorizationService.canModifyExpense(expenseId, "admin@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canModifyExpense_WithFinanceRole_ShouldReturnTrue() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("finance@example.com"))
                .thenReturn(Optional.of(financeUser));

        // When
        boolean result =
                expenseAuthorizationService.canModifyExpense(expenseId, "finance@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canModifyExpense_WithNonOwnerAndNoSpecialRole_ShouldReturnFalse() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

        // When
        boolean result =
                expenseAuthorizationService.canModifyExpense(expenseId, "other@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canModifyExpense_WithNonExistentExpense_ShouldReturnFalse() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        // When
        boolean result =
                expenseAuthorizationService.canModifyExpense(expenseId, "test@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canModifyExpense_WithNonExistentUser_ShouldReturnFalse() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        boolean result =
                expenseAuthorizationService.canModifyExpense(expenseId, "nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canViewExpense_WithOwner_ShouldReturnTrue() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        boolean result = expenseAuthorizationService.canViewExpense(expenseId, "test@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canViewExpense_WithManagerRole_ShouldReturnTrue() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(managerUser));

        // When
        boolean result =
                expenseAuthorizationService.canViewExpense(expenseId, "manager@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canViewExpense_WithFinanceRole_ShouldReturnTrue() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("finance@example.com"))
                .thenReturn(Optional.of(financeUser));

        // When
        boolean result =
                expenseAuthorizationService.canViewExpense(expenseId, "finance@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canViewExpense_WithAdminRole_ShouldReturnTrue() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // When
        boolean result = expenseAuthorizationService.canViewExpense(expenseId, "admin@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canViewExpense_WithNonOwnerAndNoSpecialRole_ShouldReturnFalse() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

        // When
        boolean result = expenseAuthorizationService.canViewExpense(expenseId, "other@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canViewExpense_WithNonExistentExpense_ShouldReturnFalse() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        // When
        boolean result = expenseAuthorizationService.canViewExpense(expenseId, "test@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canViewExpense_WithNonExistentUser_ShouldReturnFalse() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        boolean result =
                expenseAuthorizationService.canViewExpense(expenseId, "nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canModifySegments_ShouldDelegateToCanModifyExpense() {
        // Given
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(testExpense));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        boolean result =
                expenseAuthorizationService.canModifySegments(expenseId, "test@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canManageCategories_WithManagerRole_ShouldReturnTrue() {
        // Given
        when(userRepository.findByEmail("manager@example.com"))
                .thenReturn(Optional.of(managerUser));

        // When
        boolean result = expenseAuthorizationService.canManageCategories("manager@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canManageCategories_WithFinanceRole_ShouldReturnTrue() {
        // Given
        when(userRepository.findByEmail("finance@example.com"))
                .thenReturn(Optional.of(financeUser));

        // When
        boolean result = expenseAuthorizationService.canManageCategories("finance@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canManageCategories_WithAdminRole_ShouldReturnTrue() {
        // Given
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // When
        boolean result = expenseAuthorizationService.canManageCategories("admin@example.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canManageCategories_WithEmployeeRole_ShouldReturnFalse() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        boolean result = expenseAuthorizationService.canManageCategories("test@example.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canManageCategories_WithNonExistentUser_ShouldReturnFalse() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        boolean result = expenseAuthorizationService.canManageCategories("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
    }
}
