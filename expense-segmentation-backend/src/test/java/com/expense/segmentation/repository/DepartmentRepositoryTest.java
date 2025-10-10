package com.expense.segmentation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.expense.segmentation.model.Department;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class DepartmentRepositoryTest {

    @Autowired private TestEntityManager entityManager;

    @Autowired private DepartmentRepository departmentRepository;

    private Department itDepartment;
    private Department hrDepartment;

    @BeforeEach
    void setUp() {
        itDepartment = new Department();
        itDepartment.setName("IT Department");
        itDepartment.setCode("IT");
        itDepartment = entityManager.persist(itDepartment);

        hrDepartment = new Department();
        hrDepartment.setName("HR Department");
        hrDepartment.setCode("HR");
        hrDepartment = entityManager.persist(hrDepartment);

        entityManager.flush();
    }

    @Test
    void findByCode_WithExistingCode_ShouldReturnDepartment() {
        // When
        Optional<Department> found = departmentRepository.findByCode("IT");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("IT Department");
        assertThat(found.get().getCode()).isEqualTo("IT");
    }

    @Test
    void findByCode_WithNonExistingCode_ShouldReturnEmpty() {
        // When
        Optional<Department> found = departmentRepository.findByCode("SALES");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByName_WithExistingName_ShouldReturnDepartment() {
        // When
        Optional<Department> found = departmentRepository.findByName("HR Department");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("HR");
    }

    @Test
    void findByName_WithNonExistingName_ShouldReturnEmpty() {
        // When
        Optional<Department> found = departmentRepository.findByName("Sales Department");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByCode_WithExistingCode_ShouldReturnTrue() {
        // When
        boolean exists = departmentRepository.existsByCode("IT");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_WithNonExistingCode_ShouldReturnFalse() {
        // When
        boolean exists = departmentRepository.existsByCode("SALES");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByName_WithExistingName_ShouldReturnTrue() {
        // When
        boolean exists = departmentRepository.existsByName("IT Department");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_WithNonExistingName_ShouldReturnFalse() {
        // When
        boolean exists = departmentRepository.existsByName("Sales Department");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistDepartment() {
        // Given
        Department salesDepartment = new Department();
        salesDepartment.setName("Sales Department");
        salesDepartment.setCode("SALES");

        // When
        Department saved = departmentRepository.save(salesDepartment);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findAll_ShouldReturnAllDepartments() {
        // When
        var departments = departmentRepository.findAll();

        // Then
        assertThat(departments).hasSize(2);
        assertThat(departments)
                .extracting(Department::getCode)
                .containsExactlyInAnyOrder("IT", "HR");
    }

    @Test
    void delete_ShouldRemoveDepartment() {
        // When
        departmentRepository.delete(itDepartment);
        entityManager.flush();

        Optional<Department> found = departmentRepository.findByCode("IT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void update_ShouldModifyDepartment() {
        // Given
        itDepartment.setName("Information Technology");

        // When
        Department updated = departmentRepository.save(itDepartment);
        entityManager.flush();

        // Then
        Optional<Department> found = departmentRepository.findByCode("IT");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Information Technology");
        assertThat(found.get().getUpdatedAt()).isAfter(found.get().getCreatedAt());
    }
}
