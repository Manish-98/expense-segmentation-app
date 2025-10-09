package com.expense.segmentation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.expense.segmentation.dto.AttachmentResponse;
import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseAttachment;
import com.expense.segmentation.model.User;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttachmentMapperTest {

    private AttachmentMapper attachmentMapper;

    @BeforeEach
    void setUp() {
        attachmentMapper = new AttachmentMapper();
    }

    @Test
    void toResponse_WithValidAttachment_ShouldMapAllFields() {
        // Arrange
        UUID attachmentId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime uploadedAt = LocalDateTime.now();

        User user = new User();
        user.setId(userId);
        user.setEmail("john.doe@example.com");
        user.setName("John Doe");

        Expense expense = new Expense();
        expense.setId(expenseId);

        ExpenseAttachment attachment = new ExpenseAttachment();
        attachment.setId(attachmentId);
        attachment.setExpense(expense);
        attachment.setFilename("test-file.pdf");
        attachment.setOriginalFilename("original-file.pdf");
        attachment.setMimeType("application/pdf");
        attachment.setFileSize(1024L);
        attachment.setUploadedBy(user);
        attachment.setUploadedAt(uploadedAt);

        // Act
        AttachmentResponse response = attachmentMapper.toResponse(attachment);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(attachmentId);
        assertThat(response.getExpenseId()).isEqualTo(expenseId);
        assertThat(response.getFilename()).isEqualTo("test-file.pdf");
        assertThat(response.getOriginalFilename()).isEqualTo("original-file.pdf");
        assertThat(response.getMimeType()).isEqualTo("application/pdf");
        assertThat(response.getFileSize()).isEqualTo(1024L);
        assertThat(response.getUploadedByEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getUploadedByName()).isEqualTo("John Doe");
        assertThat(response.getUploadedAt()).isEqualTo(uploadedAt);
    }

    @Test
    void toResponse_WithDifferentFileTypes_ShouldMapCorrectly() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");

        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());

        ExpenseAttachment attachment = new ExpenseAttachment();
        attachment.setId(UUID.randomUUID());
        attachment.setExpense(expense);
        attachment.setFilename("image.jpg");
        attachment.setOriginalFilename("photo.jpg");
        attachment.setMimeType("image/jpeg");
        attachment.setFileSize(2048L);
        attachment.setUploadedBy(user);
        attachment.setUploadedAt(LocalDateTime.now());

        // Act
        AttachmentResponse response = attachmentMapper.toResponse(attachment);

        // Assert
        assertThat(response.getMimeType()).isEqualTo("image/jpeg");
        assertThat(response.getFilename()).isEqualTo("image.jpg");
        assertThat(response.getOriginalFilename()).isEqualTo("photo.jpg");
    }

    @Test
    void toResponse_WithLargeFile_ShouldMapFileSizeCorrectly() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");

        Expense expense = new Expense();
        expense.setId(UUID.randomUUID());

        ExpenseAttachment attachment = new ExpenseAttachment();
        attachment.setId(UUID.randomUUID());
        attachment.setExpense(expense);
        attachment.setFilename("large-file.pdf");
        attachment.setOriginalFilename("large-file.pdf");
        attachment.setMimeType("application/pdf");
        attachment.setFileSize(10485760L); // 10MB
        attachment.setUploadedBy(user);
        attachment.setUploadedAt(LocalDateTime.now());

        // Act
        AttachmentResponse response = attachmentMapper.toResponse(attachment);

        // Assert
        assertThat(response.getFileSize()).isEqualTo(10485760L);
    }
}
