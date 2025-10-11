package com.expense.segmentation.repository;

import com.expense.segmentation.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByNameAndActive(String name, Boolean active);

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.name")
    List<Category> findByActiveTrueOrderByName();

    boolean existsByNameAndActive(String name, Boolean active);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.active = true")
    long countActiveCategories();
}
