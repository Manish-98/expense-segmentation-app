package com.expense.segmentation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired private TestEntityManager entityManager;

    @Autowired private RoleRepository roleRepository;

    private Role employeeRole;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        employeeRole = new Role();
        employeeRole.setName(RoleType.EMPLOYEE);
        employeeRole.setDescription("Employee role");
        employeeRole = entityManager.persist(employeeRole);

        managerRole = new Role();
        managerRole.setName(RoleType.MANAGER);
        managerRole.setDescription("Manager role");
        managerRole = entityManager.persist(managerRole);

        entityManager.flush();
    }

    @Test
    void findByName_WithExistingRole_ShouldReturnRole() {
        // When
        Optional<Role> found = roleRepository.findByName(RoleType.EMPLOYEE);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(RoleType.EMPLOYEE);
        assertThat(found.get().getDescription()).isEqualTo("Employee role");
    }

    @Test
    void findByName_WithNonExistingRole_ShouldReturnEmpty() {
        // When
        Optional<Role> found = roleRepository.findByName(RoleType.ADMIN);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByName_WithExistingRole_ShouldReturnTrue() {
        // When
        boolean exists = roleRepository.existsByName(RoleType.EMPLOYEE);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_WithNonExistingRole_ShouldReturnFalse() {
        // When
        boolean exists = roleRepository.existsByName(RoleType.FINANCE);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistRole() {
        // Given
        Role adminRole = new Role();
        adminRole.setName(RoleType.ADMIN);
        adminRole.setDescription("Admin role");

        // When
        Role saved = roleRepository.save(adminRole);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(RoleType.ADMIN);
    }

    @Test
    void findAll_ShouldReturnAllRoles() {
        // When
        var roles = roleRepository.findAll();

        // Then
        assertThat(roles).hasSize(2);
        assertThat(roles)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleType.EMPLOYEE, RoleType.MANAGER);
    }

    @Test
    void delete_ShouldRemoveRole() {
        // When
        roleRepository.delete(employeeRole);
        entityManager.flush();

        Optional<Role> found = roleRepository.findByName(RoleType.EMPLOYEE);

        // Then
        assertThat(found).isEmpty();
    }
}
