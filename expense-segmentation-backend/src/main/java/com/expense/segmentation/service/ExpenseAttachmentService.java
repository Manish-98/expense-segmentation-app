package com.expense.segmentation.service;

import com.expense.segmentation.dto.AttachmentResponse;
import com.expense.segmentation.exception.InvalidOperationException;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.AttachmentMapper;
import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseAttachment;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.repository.ExpenseAttachmentRepository;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.UserRepository;
import com.expense.segmentation.service.storage.FileStorageService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseAttachmentService {

    private final ExpenseAttachmentRepository attachmentRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AttachmentMapper attachmentMapper;

    @Value("${file.max-size:10485760}") // 10MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_MIME_TYPES =
            Arrays.asList("application/pdf", "image/jpeg", "image/jpg", "image/png");

    @Transactional
    public AttachmentResponse uploadAttachment(UUID expenseId, MultipartFile file) {
        log.info("Uploading attachment for expense: {}", expenseId);

        // Validate file
        validateFile(file);

        // Get current user
        User currentUser = getCurrentUser();

        // Get expense
        Expense expense =
                expenseRepository
                        .findByIdWithCreatedBy(expenseId)
                        .orElseThrow(
                                () -> {
                                    log.error("Expense not found: {}", expenseId);
                                    return new ResourceNotFoundException(
                                            "Expense", expenseId.toString());
                                });

        // Check upload authorization - only creator or finance/admin can upload
        checkUploadAuthorization(expense, currentUser);

        // Store file
        String storedPath = fileStorageService.storeFile(file, expenseId.toString());

        // Create attachment entity
        ExpenseAttachment attachment = new ExpenseAttachment();
        attachment.setExpense(expense);
        attachment.setFilename(file.getOriginalFilename());
        attachment.setOriginalFilename(file.getOriginalFilename());
        attachment.setStoredPath(storedPath);
        attachment.setMimeType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setUploadedBy(currentUser);

        // Save to database
        ExpenseAttachment saved = attachmentRepository.save(attachment);
        log.info("Attachment uploaded successfully: {}", saved.getId());

        return attachmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByExpense(UUID expenseId) {
        log.debug("Fetching attachments for expense: {}", expenseId);

        // Get current user
        User currentUser = getCurrentUser();

        // Verify expense exists and check authorization
        getExpenseWithAuthorization(expenseId, currentUser);

        // Fetch attachments
        List<ExpenseAttachment> attachments =
                attachmentRepository.findByExpenseIdWithUploadedBy(expenseId);

        log.info("Retrieved {} attachments for expense: {}", attachments.size(), expenseId);
        return attachments.stream().map(attachmentMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadAttachment(UUID attachmentId) {
        log.debug("Downloading attachment: {}", attachmentId);

        // Get current user
        User currentUser = getCurrentUser();

        // Fetch attachment with details
        ExpenseAttachment attachment =
                attachmentRepository
                        .findByIdWithDetails(attachmentId)
                        .orElseThrow(
                                () -> {
                                    log.error("Attachment not found: {}", attachmentId);
                                    return new ResourceNotFoundException(
                                            "Attachment", attachmentId.toString());
                                });

        // Check authorization
        checkAccessAuthorization(attachment.getExpense(), currentUser);

        // Load file
        Resource resource = fileStorageService.loadFileAsResource(attachment.getStoredPath());
        log.info("Attachment downloaded successfully: {}", attachmentId);
        return resource;
    }

    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        log.info("Deleting attachment: {}", attachmentId);

        // Get current user
        User currentUser = getCurrentUser();

        // Fetch attachment with details
        ExpenseAttachment attachment =
                attachmentRepository
                        .findByIdWithDetails(attachmentId)
                        .orElseThrow(
                                () -> {
                                    log.error("Attachment not found: {}", attachmentId);
                                    return new ResourceNotFoundException(
                                            "Attachment", attachmentId.toString());
                                });

        // Check authorization - only the uploader or finance/admin can delete
        RoleType currentUserRole = currentUser.getRole().getName();
        boolean isFinanceOrAdmin =
                RoleType.FINANCE.equals(currentUserRole) || RoleType.ADMIN.equals(currentUserRole);

        if (!isFinanceOrAdmin && !currentUser.getId().equals(attachment.getUploadedBy().getId())) {
            log.warn(
                    "User {} attempted to delete attachment {} without permission",
                    currentUser.getId(),
                    attachmentId);
            throw new SecurityException("You are not authorized to delete this attachment");
        }

        // Delete file from storage
        fileStorageService.deleteFile(attachment.getStoredPath());

        // Delete from database
        attachmentRepository.delete(attachment);
        log.info("Attachment deleted successfully: {}", attachmentId);
    }

    @Transactional(readOnly = true)
    public ExpenseAttachment getAttachmentById(UUID attachmentId) {
        return attachmentRepository
                .findByIdWithDetails(attachmentId)
                .orElseThrow(
                        () -> {
                            log.error("Attachment not found: {}", attachmentId);
                            return new ResourceNotFoundException(
                                    "Attachment", attachmentId.toString());
                        });
    }

    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new InvalidOperationException("Cannot upload empty file");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new InvalidOperationException(
                    "File size exceeds maximum allowed size of "
                            + (maxFileSize / 1024 / 1024)
                            + "MB");
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidOperationException(
                    "File type not allowed. Allowed types: PDF, JPEG, JPG, PNG");
        }

        // Additional filename validation
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            throw new InvalidOperationException("Invalid filename");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        return userRepository
                .findByEmail(userEmail)
                .orElseThrow(
                        () -> {
                            log.error("Authenticated user not found: {}", userEmail);
                            return new ResourceNotFoundException("User", "email", userEmail);
                        });
    }

    private Expense getExpenseWithAuthorization(UUID expenseId, User currentUser) {
        Expense expense =
                expenseRepository
                        .findByIdWithCreatedBy(expenseId)
                        .orElseThrow(
                                () -> {
                                    log.error("Expense not found: {}", expenseId);
                                    return new ResourceNotFoundException(
                                            "Expense", expenseId.toString());
                                });

        checkAccessAuthorization(expense, currentUser);
        return expense;
    }

    private void checkAccessAuthorization(Expense expense, User currentUser) {
        RoleType currentUserRole = currentUser.getRole().getName();
        boolean isFinanceOrAdmin =
                RoleType.FINANCE.equals(currentUserRole) || RoleType.ADMIN.equals(currentUserRole);

        if (!isFinanceOrAdmin && !currentUser.getId().equals(expense.getCreatedBy().getId())) {
            log.warn(
                    "User {} attempted to access expense {} attachments without permission",
                    currentUser.getId(),
                    expense.getId());
            throw new SecurityException(
                    "You are not authorized to access attachments for this expense");
        }
    }

    private void checkUploadAuthorization(Expense expense, User currentUser) {
        RoleType currentUserRole = currentUser.getRole().getName();
        boolean isFinanceOrAdmin =
                RoleType.FINANCE.equals(currentUserRole) || RoleType.ADMIN.equals(currentUserRole);

        if (!isFinanceOrAdmin && !currentUser.getId().equals(expense.getCreatedBy().getId())) {
            log.warn(
                    "User {} attempted to upload attachment to expense {} without permission",
                    currentUser.getId(),
                    expense.getId());
            throw new SecurityException(
                    "You are not authorized to upload attachments for this expense. Only the"
                            + " expense creator can add attachments.");
        }
    }
}
