package com.expense.segmentation.mapper;

import com.expense.segmentation.dto.AttachmentResponse;
import com.expense.segmentation.model.ExpenseAttachment;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentResponse toResponse(ExpenseAttachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .expenseId(attachment.getExpense().getId())
                .filename(attachment.getFilename())
                .originalFilename(attachment.getOriginalFilename())
                .mimeType(attachment.getMimeType())
                .fileSize(attachment.getFileSize())
                .uploadedByEmail(attachment.getUploadedBy().getEmail())
                .uploadedByName(
                        attachment.getUploadedBy().getFirstName()
                                + " "
                                + attachment.getUploadedBy().getLastName())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}
