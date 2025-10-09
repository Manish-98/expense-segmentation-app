package com.expense.segmentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.expense.segmentation.config.JwtAuthenticationFilter;
import com.expense.segmentation.config.JwtTokenUtil;
import com.expense.segmentation.dto.AttachmentResponse;
import com.expense.segmentation.model.ExpenseAttachment;
import com.expense.segmentation.service.CustomUserDetailsService;
import com.expense.segmentation.service.ExpenseAttachmentService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExpenseAttachmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseAttachmentControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ExpenseAttachmentService attachmentService;

    @MockBean private JwtTokenUtil jwtTokenUtil;

    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean private CustomUserDetailsService customUserDetailsService;

    private UUID expenseId;
    private UUID attachmentId;
    private AttachmentResponse attachmentResponse;
    private ExpenseAttachment expenseAttachment;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        attachmentId = UUID.randomUUID();

        attachmentResponse = AttachmentResponse.builder()
                .id(attachmentId)
                .expenseId(expenseId)
                .filename("test-file.pdf")
                .originalFilename("original-file.pdf")
                .mimeType("application/pdf")
                .fileSize(1024L)
                .uploadedByEmail("test@example.com")
                .uploadedByName("Test User")
                .uploadedAt(LocalDateTime.now())
                .build();

        expenseAttachment = new ExpenseAttachment();
        expenseAttachment.setId(attachmentId);
        expenseAttachment.setFilename("test-file.pdf");
        expenseAttachment.setOriginalFilename("original-file.pdf");
        expenseAttachment.setMimeType("application/pdf");
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void uploadAttachment_WithEmployeeRole_ShouldUploadSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        "application/pdf",
                        "test content".getBytes());

        when(attachmentService.uploadAttachment(eq(expenseId), any())).thenReturn(attachmentResponse);

        // Act & Assert
        mockMvc.perform(
                        multipart("/expenses/{expenseId}/attachments", expenseId)
                                .file(file)
                                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(attachmentId.toString()))
                .andExpect(jsonPath("$.expenseId").value(expenseId.toString()))
                .andExpect(jsonPath("$.filename").value("test-file.pdf"))
                .andExpect(jsonPath("$.originalFilename").value("original-file.pdf"))
                .andExpect(jsonPath("$.mimeType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").value(1024))
                .andExpect(jsonPath("$.uploadedByEmail").value("test@example.com"))
                .andExpect(jsonPath("$.uploadedByName").value("Test User"));

        verify(attachmentService).uploadAttachment(eq(expenseId), any());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void uploadAttachment_WithFinanceRole_ShouldUploadSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        "application/pdf",
                        "test content".getBytes());

        when(attachmentService.uploadAttachment(eq(expenseId), any())).thenReturn(attachmentResponse);

        // Act & Assert
        mockMvc.perform(
                        multipart("/expenses/{expenseId}/attachments", expenseId)
                                .file(file)
                                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadAttachment_WithAdminRole_ShouldUploadSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        "application/pdf",
                        "test content".getBytes());

        when(attachmentService.uploadAttachment(eq(expenseId), any())).thenReturn(attachmentResponse);

        // Act & Assert
        mockMvc.perform(
                        multipart("/expenses/{expenseId}/attachments", expenseId)
                                .file(file)
                                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void listAttachments_WithEmployeeRole_ShouldReturnAttachments() throws Exception {
        // Arrange
        AttachmentResponse attachment1 = AttachmentResponse.builder()
                .id(UUID.randomUUID())
                .expenseId(expenseId)
                .filename("file1.pdf")
                .originalFilename("file1.pdf")
                .mimeType("application/pdf")
                .fileSize(1024L)
                .uploadedByEmail("test@example.com")
                .uploadedByName("Test User")
                .uploadedAt(LocalDateTime.now())
                .build();

        AttachmentResponse attachment2 = AttachmentResponse.builder()
                .id(UUID.randomUUID())
                .expenseId(expenseId)
                .filename("file2.pdf")
                .originalFilename("file2.pdf")
                .mimeType("application/pdf")
                .fileSize(2048L)
                .uploadedByEmail("test@example.com")
                .uploadedByName("Test User")
                .uploadedAt(LocalDateTime.now())
                .build();

        List<AttachmentResponse> attachments = Arrays.asList(attachment1, attachment2);

        when(attachmentService.getAttachmentsByExpense(expenseId)).thenReturn(attachments);

        // Act & Assert
        mockMvc.perform(get("/expenses/{expenseId}/attachments", expenseId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].filename").value("file1.pdf"))
                .andExpect(jsonPath("$[1].filename").value("file2.pdf"));

        verify(attachmentService).getAttachmentsByExpense(expenseId);
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void listAttachments_WithFinanceRole_ShouldReturnAttachments() throws Exception {
        // Arrange
        when(attachmentService.getAttachmentsByExpense(expenseId))
                .thenReturn(Arrays.asList(attachmentResponse));

        // Act & Assert
        mockMvc.perform(get("/expenses/{expenseId}/attachments", expenseId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void downloadAttachment_WithEmployeeRole_ShouldReturnFile() throws Exception {
        // Arrange
        Resource resource = new ByteArrayResource("file content".getBytes());

        when(attachmentService.downloadAttachment(attachmentId)).thenReturn(resource);
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(expenseAttachment);

        // Act & Assert
        mockMvc.perform(
                        get(
                                        "/expenses/{expenseId}/attachments/{attachmentId}",
                                        expenseId,
                                        attachmentId)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(
                        header()
                                .string(
                                        "Content-Disposition",
                                        "attachment; filename=\"original-file.pdf\""));

        verify(attachmentService).downloadAttachment(attachmentId);
        verify(attachmentService).getAttachmentById(attachmentId);
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void downloadAttachment_WithFinanceRole_ShouldReturnFile() throws Exception {
        // Arrange
        Resource resource = new ByteArrayResource("file content".getBytes());

        when(attachmentService.downloadAttachment(attachmentId)).thenReturn(resource);
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(expenseAttachment);

        // Act & Assert
        mockMvc.perform(
                        get(
                                        "/expenses/{expenseId}/attachments/{attachmentId}",
                                        expenseId,
                                        attachmentId)
                                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deleteAttachment_WithEmployeeRole_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        doNothing().when(attachmentService).deleteAttachment(attachmentId);

        // Act & Assert
        mockMvc.perform(
                        delete(
                                        "/expenses/{expenseId}/attachments/{attachmentId}",
                                        expenseId,
                                        attachmentId)
                                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(attachmentService).deleteAttachment(attachmentId);
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void deleteAttachment_WithFinanceRole_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        doNothing().when(attachmentService).deleteAttachment(attachmentId);

        // Act & Assert
        mockMvc.perform(
                        delete(
                                        "/expenses/{expenseId}/attachments/{attachmentId}",
                                        expenseId,
                                        attachmentId)
                                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(attachmentService).deleteAttachment(attachmentId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAttachment_WithAdminRole_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        doNothing().when(attachmentService).deleteAttachment(attachmentId);

        // Act & Assert
        mockMvc.perform(
                        delete(
                                        "/expenses/{expenseId}/attachments/{attachmentId}",
                                        expenseId,
                                        attachmentId)
                                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(attachmentService).deleteAttachment(attachmentId);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void uploadAttachment_WithJpegImage_ShouldUploadSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile imageFile =
                new MockMultipartFile(
                        "file",
                        "photo.jpg",
                        "image/jpeg",
                        "image content".getBytes());

        AttachmentResponse imageResponse = AttachmentResponse.builder()
                .id(UUID.randomUUID())
                .expenseId(expenseId)
                .filename("photo.jpg")
                .originalFilename("photo.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .uploadedByEmail("test@example.com")
                .uploadedByName("Test User")
                .uploadedAt(LocalDateTime.now())
                .build();

        when(attachmentService.uploadAttachment(eq(expenseId), any())).thenReturn(imageResponse);

        // Act & Assert
        mockMvc.perform(
                        multipart("/expenses/{expenseId}/attachments", expenseId)
                                .file(imageFile)
                                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mimeType").value("image/jpeg"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void uploadAttachment_WithPngImage_ShouldUploadSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile imageFile =
                new MockMultipartFile(
                        "file",
                        "screenshot.png",
                        "image/png",
                        "image content".getBytes());

        AttachmentResponse imageResponse = AttachmentResponse.builder()
                .id(UUID.randomUUID())
                .expenseId(expenseId)
                .filename("screenshot.png")
                .originalFilename("screenshot.png")
                .mimeType("image/png")
                .fileSize(1024L)
                .uploadedByEmail("test@example.com")
                .uploadedByName("Test User")
                .uploadedAt(LocalDateTime.now())
                .build();

        when(attachmentService.uploadAttachment(eq(expenseId), any())).thenReturn(imageResponse);

        // Act & Assert
        mockMvc.perform(
                        multipart("/expenses/{expenseId}/attachments", expenseId)
                                .file(imageFile)
                                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mimeType").value("image/png"));
    }
}
