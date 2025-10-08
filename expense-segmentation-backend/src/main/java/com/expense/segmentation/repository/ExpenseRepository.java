package com.expense.segmentation.repository;

import com.expense.segmentation.model.Expense;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    /**
     * Fetches all expenses with their creator (user) eagerly loaded using JOIN FETCH. This prevents
     * N+1 query problems when accessing expense.createdBy.
     *
     * @return list of all expenses with eagerly loaded creator
     */
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.createdBy")
    List<Expense> findAllWithCreatedBy();

    /**
     * Fetches an expense by ID with its creator (user) eagerly loaded using JOIN FETCH. This
     * prevents N+1 query problems when accessing expense.createdBy.
     *
     * @param id the expense ID
     * @return optional containing the expense with eagerly loaded creator
     */
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.createdBy WHERE e.id = :id")
    Optional<Expense> findByIdWithCreatedBy(@Param("id") UUID id);

    /**
     * Fetches all expenses created by a specific user.
     *
     * @param userId the user ID
     * @return list of expenses created by the user
     */
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.createdBy WHERE e.createdBy.id = :userId")
    List<Expense> findByCreatedById(@Param("userId") UUID userId);
}
