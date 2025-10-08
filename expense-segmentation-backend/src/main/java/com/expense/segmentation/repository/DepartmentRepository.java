package com.expense.segmentation.repository;

import com.expense.segmentation.model.Department;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByCode(String code);

    Optional<Department> findByName(String name);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    /**
     * Fetches all departments with their manager eagerly loaded using JOIN FETCH. This prevents N+1
     * query problems when accessing department.manager.
     *
     * @return list of all departments with eagerly loaded manager
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.manager")
    List<Department> findAllWithManager();

    /**
     * Fetches a department by ID with its manager eagerly loaded using JOIN FETCH. This prevents
     * N+1 query problems when accessing department.manager.
     *
     * @param id the department ID
     * @return optional containing the department with eagerly loaded manager
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.manager WHERE d.id = :id")
    Optional<Department> findByIdWithManager(@Param("id") UUID id);
}
