package com.expense.segmentation.repository;

import com.expense.segmentation.model.ExpenseAttachment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseAttachmentRepository extends JpaRepository<ExpenseAttachment, UUID> {

    @Query(
            "SELECT ea FROM ExpenseAttachment ea "
                    + "LEFT JOIN FETCH ea.uploadedBy "
                    + "WHERE ea.expense.id = :expenseId "
                    + "ORDER BY ea.uploadedAt DESC")
    List<ExpenseAttachment> findByExpenseIdWithUploadedBy(@Param("expenseId") UUID expenseId);

    @Query(
            "SELECT ea FROM ExpenseAttachment ea "
                    + "LEFT JOIN FETCH ea.expense "
                    + "LEFT JOIN FETCH ea.uploadedBy "
                    + "WHERE ea.id = :id")
    java.util.Optional<ExpenseAttachment> findByIdWithDetails(@Param("id") UUID id);

    void deleteByExpenseId(UUID expenseId);
}
