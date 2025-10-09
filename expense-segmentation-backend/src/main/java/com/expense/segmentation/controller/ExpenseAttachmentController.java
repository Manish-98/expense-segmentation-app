package com.expense.segmentation.controller;

import com.expense.segmentation.dto.AttachmentResponse;
import com.expense.segmentation.model.ExpenseAttachment;
import com.expense.segmentation.service.ExpenseAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/expenses/{expenseId}/attachments")
@RequiredArgsConstructor
@Tag(name = "Expense Attachments", description = "APIs for managing expense attachments")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseAttachmentController {

    private final ExpenseAttachmentService attachmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN')")
    @Operation(
            summary = "Upload attachment",
            description = "Upload a file attachment for an expense (PDF, JPG, JPEG, PNG)")
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file) {
        log.info("POST /expenses/{}/attachments - Uploading file", expenseId);
        AttachmentResponse response = attachmentService.uploadAttachment(expenseId, file);
        log.info("POST /expenses/{}/attachments - File uploaded successfully", expenseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN')")
    @Operation(
            summary = "List attachments",
            description = "Get all attachments for an expense")
    public ResponseEntity<List<AttachmentResponse>> listAttachments(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId) {
        log.info("GET /expenses/{}/attachments - Listing attachments", expenseId);
        List<AttachmentResponse> attachments = attachmentService.getAttachmentsByExpense(expenseId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{attachmentId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN')")
    @Operation(
            summary = "Download attachment",
            description = "Download a specific attachment file")
    public ResponseEntity<Resource> downloadAttachment(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
            @Parameter(description = "Attachment ID") @PathVariable UUID attachmentId) {
        log.info(
                "GET /expenses/{}/attachments/{} - Downloading attachment", expenseId, attachmentId);

        Resource resource = attachmentService.downloadAttachment(attachmentId);
        ExpenseAttachment attachment = attachmentService.getAttachmentById(attachmentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'FINANCE', 'ADMIN')")
    @Operation(
            summary = "Delete attachment",
            description = "Delete a specific attachment (only uploader, finance, or admin)")
    public ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
            @Parameter(description = "Attachment ID") @PathVariable UUID attachmentId) {
        log.info(
                "DELETE /expenses/{}/attachments/{} - Deleting attachment", expenseId, attachmentId);
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
}
