package com.expense.segmentation.repository;

import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseStatus;
import com.expense.segmentation.model.ExpenseType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    /**
     * Fetches all expenses with their creator (user) eagerly loaded using JOIN FETCH. This
     * prevents N+1 query problems when accessing expense.createdBy.
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

    /**
     * Fetches expenses with pagination and filtering support. Count query is separate to avoid
     * JOIN FETCH in count queries.
     *
     * @param userId optional user ID filter (null to get all)
     * @param dateFrom optional start date filter
     * @param dateTo optional end date filter
     * @param type optional expense type filter
     * @param status optional expense status filter
     * @param pageable pagination parameters
     * @return page of expenses with eagerly loaded creator
     */
    @Query(
            value =
                    "SELECT e FROM Expense e LEFT JOIN FETCH e.createdBy "
                            + "WHERE (:userId IS NULL OR e.createdBy.id = :userId) "
                            + "AND (:dateFrom IS NULL OR e.date >= :dateFrom) "
                            + "AND (:dateTo IS NULL OR e.date <= :dateTo) "
                            + "AND (:type IS NULL OR e.type = :type) "
                            + "AND (:status IS NULL OR e.status = :status) "
                            + "ORDER BY e.date DESC, e.createdAt DESC",
            countQuery =
                    "SELECT COUNT(e) FROM Expense e "
                            + "WHERE (:userId IS NULL OR e.createdBy.id = :userId) "
                            + "AND (:dateFrom IS NULL OR e.date >= :dateFrom) "
                            + "AND (:dateTo IS NULL OR e.date <= :dateTo) "
                            + "AND (:type IS NULL OR e.type = :type) "
                            + "AND (:status IS NULL OR e.status = :status)")
    Page<Expense> findExpensesWithFilters(
            @Param("userId") UUID userId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("type") ExpenseType type,
            @Param("status") ExpenseStatus status,
            Pageable pageable);
}
