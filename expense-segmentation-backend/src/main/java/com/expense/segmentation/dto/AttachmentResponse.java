package com.expense.segmentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private UUID id;
    private UUID expenseId;
    private String filename;
    private String originalFilename;
    private String mimeType;
    private Long fileSize;
    private String uploadedByEmail;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
}
