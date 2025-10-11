package com.expense.segmentation.repository;

import com.expense.segmentation.model.ExpenseSegment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseSegmentRepository extends JpaRepository<ExpenseSegment, UUID> {

    List<ExpenseSegment> findByExpenseId(UUID expenseId);

    @Query("SELECT es FROM ExpenseSegment es WHERE es.expense.id = :expenseId ORDER BY es.category")
    List<ExpenseSegment> findByExpenseIdOrderByCategory(@Param("expenseId") UUID expenseId);

    @Query(
            "SELECT es FROM ExpenseSegment es WHERE es.expense.id = :expenseId AND es.id ="
                    + " :segmentId")
    Optional<ExpenseSegment> findByExpenseIdAndId(
            @Param("expenseId") UUID expenseId, @Param("segmentId") UUID segmentId);

    void deleteByExpenseId(UUID expenseId);

    void deleteByExpenseIdAndId(UUID expenseId, UUID segmentId);
}
