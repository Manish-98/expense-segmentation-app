package com.expense.segmentation.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock private RoleRepository roleRepository;

    @InjectMocks private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    void run_ShouldInitializeRoles() {
        // Given
        when(roleRepository.existsByName(any(RoleType.class))).thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataInitializer.run();

        // Then
        verify(roleRepository, times(RoleType.values().length)).existsByName(any(RoleType.class));
        verify(roleRepository, times(RoleType.values().length)).save(any(Role.class));
    }

    @Test
    void initializeRoles_WhenRolesDoNotExist_ShouldCreateAllRoles() {
        // Given
        when(roleRepository.existsByName(any(RoleType.class))).thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataInitializer.run();

        // Then
        verify(roleRepository).existsByName(RoleType.EMPLOYEE);
        verify(roleRepository).existsByName(RoleType.MANAGER);
        verify(roleRepository).existsByName(RoleType.FINANCE);
        verify(roleRepository).existsByName(RoleType.ADMIN);
        verify(roleRepository).existsByName(RoleType.OWNER);

        verify(roleRepository, times(5)).save(any(Role.class));
    }

    @Test
    void initializeRoles_WhenRolesAlreadyExist_ShouldNotCreateDuplicates() {
        // Given
        when(roleRepository.existsByName(any(RoleType.class))).thenReturn(true);

        // When
        dataInitializer.run();

        // Then
        verify(roleRepository, times(RoleType.values().length)).existsByName(any(RoleType.class));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void initializeRoles_WhenSomeRolesExist_ShouldCreateOnlyMissingRoles() {
        // Given
        when(roleRepository.existsByName(RoleType.EMPLOYEE)).thenReturn(true);
        when(roleRepository.existsByName(RoleType.MANAGER)).thenReturn(false);
        when(roleRepository.existsByName(RoleType.FINANCE)).thenReturn(true);
        when(roleRepository.existsByName(RoleType.ADMIN)).thenReturn(false);
        when(roleRepository.existsByName(RoleType.OWNER)).thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataInitializer.run();

        // Then
        verify(roleRepository, times(3)).save(any(Role.class));
    }

    @Test
    void initializeRoles_ShouldSetCorrectDescriptions() {
        // Given
        when(roleRepository.existsByName(any(RoleType.class))).thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        dataInitializer.run();

        // Then
        verify(roleRepository)
                .save(
                        argThat(
                                role ->
                                        role.getName() == RoleType.EMPLOYEE
                                                && role.getDescription()
                                                        .equals(
                                                                "Regular employee with basic"
                                                                        + " access")));

        verify(roleRepository)
                .save(
                        argThat(
                                role ->
                                        role.getName() == RoleType.MANAGER
                                                && role.getDescription()
                                                        .equals(
                                                                "Department manager with team"
                                                                        + " management access")));

        verify(roleRepository)
                .save(
                        argThat(
                                role ->
                                        role.getName() == RoleType.FINANCE
                                                && role.getDescription()
                                                        .equals(
                                                                "Finance team member with expense"
                                                                        + " approval access")));

        verify(roleRepository)
                .save(
                        argThat(
                                role ->
                                        role.getName() == RoleType.ADMIN
                                                && role.getDescription()
                                                        .equals(
                                                                "Administrator with full system"
                                                                        + " access")));

        verify(roleRepository)
                .save(
                        argThat(
                                role ->
                                        role.getName() == RoleType.OWNER
                                                && role.getDescription()
                                                        .equals(
                                                                "Business owner with complete"
                                                                        + " control")));
    }
}
