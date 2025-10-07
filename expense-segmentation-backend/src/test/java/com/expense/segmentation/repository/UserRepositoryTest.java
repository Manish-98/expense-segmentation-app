package com.expense.segmentation.repository;

import com.expense.segmentation.model.Department;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Role employeeRole;
    private Role managerRole;
    private Department itDepartment;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Create roles
        employeeRole = new Role();
        employeeRole.setName(RoleType.EMPLOYEE);
        employeeRole.setDescription("Employee");
        employeeRole = entityManager.persist(employeeRole);

        managerRole = new Role();
        managerRole.setName(RoleType.MANAGER);
        managerRole.setDescription("Manager");
        managerRole = entityManager.persist(managerRole);

        // Create department
        itDepartment = new Department();
        itDepartment.setName("IT Department");
        itDepartment.setCode("IT");
        itDepartment = entityManager.persist(itDepartment);

        // Create users
        user1 = new User();
        user1.setName("John Doe");
        user1.setEmail("john@example.com");
        user1.setPasswordHash("password123");
        user1.setRole(employeeRole);
        user1.setDepartment(itDepartment);
        user1.setStatus(UserStatus.ACTIVE);
        user1 = entityManager.persist(user1);

        user2 = new User();
        user2.setName("Jane Smith");
        user2.setEmail("jane@example.com");
        user2.setPasswordHash("password456");
        user2.setRole(managerRole);
        user2.setDepartment(itDepartment);
        user2.setStatus(UserStatus.INACTIVE);
        user2 = entityManager.persist(user2);

        entityManager.flush();
    }

    @Test
    void findByEmail_WithExistingEmail_ShouldReturnUser() {
        // When
        Optional<User> found = userRepository.findByEmail("john@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // When
        boolean exists = userRepository.existsByEmail("john@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByStatus_ShouldReturnUsersWithGivenStatus() {
        // When
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        List<User> inactiveUsers = userRepository.findByStatus(UserStatus.INACTIVE);

        // Then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("john@example.com");

        assertThat(inactiveUsers).hasSize(1);
        assertThat(inactiveUsers.get(0).getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void findByDepartmentId_ShouldReturnUsersInDepartment() {
        // When
        List<User> users = userRepository.findByDepartmentId(itDepartment.getId());

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("john@example.com", "jane@example.com");
    }

    @Test
    void findByRoleId_ShouldReturnUsersWithRole() {
        // When
        List<User> employees = userRepository.findByRoleId(employeeRole.getId());
        List<User> managers = userRepository.findByRoleId(managerRole.getId());

        // Then
        assertThat(employees).hasSize(1);
        assertThat(employees.get(0).getEmail()).isEqualTo("john@example.com");

        assertThat(managers).hasSize(1);
        assertThat(managers.get(0).getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void findByDepartmentIdAndStatus_ShouldReturnFilteredUsers() {
        // When
        List<User> activeInIT = userRepository.findByDepartmentIdAndStatus(
                itDepartment.getId(), UserStatus.ACTIVE);
        List<User> inactiveInIT = userRepository.findByDepartmentIdAndStatus(
                itDepartment.getId(), UserStatus.INACTIVE);

        // Then
        assertThat(activeInIT).hasSize(1);
        assertThat(activeInIT.get(0).getEmail()).isEqualTo("john@example.com");

        assertThat(inactiveInIT).hasSize(1);
        assertThat(inactiveInIT.get(0).getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void save_ShouldPersistUser() {
        // Given
        User newUser = new User();
        newUser.setName("Bob Wilson");
        newUser.setEmail("bob@example.com");
        newUser.setPasswordHash("password789");
        newUser.setRole(employeeRole);
        newUser.setStatus(UserStatus.ACTIVE);

        // When
        User saved = userRepository.save(newUser);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void delete_ShouldRemoveUser() {
        // When
        userRepository.delete(user1);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("john@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // When
        List<User> users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(2);
    }
}
