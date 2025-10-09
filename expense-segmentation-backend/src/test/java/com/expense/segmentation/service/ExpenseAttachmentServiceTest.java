package com.expense.segmentation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.expense.segmentation.dto.AttachmentResponse;
import com.expense.segmentation.exception.InvalidOperationException;
import com.expense.segmentation.exception.ResourceNotFoundException;
import com.expense.segmentation.mapper.AttachmentMapper;
import com.expense.segmentation.model.Expense;
import com.expense.segmentation.model.ExpenseAttachment;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.model.User;
import com.expense.segmentation.repository.ExpenseAttachmentRepository;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.UserRepository;
import com.expense.segmentation.service.storage.FileStorageService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExpenseAttachmentServiceTest {

    @Mock private ExpenseAttachmentRepository attachmentRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;
    @Mock private Resource mockResource;

    private ExpenseAttachmentService attachmentService;
    private AttachmentMapper attachmentMapper;

    private User testUser;
    private User financeUser;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        attachmentMapper = new AttachmentMapper();
        attachmentService =
                new ExpenseAttachmentService(
                        attachmentRepository,
                        expenseRepository,
                        userRepository,
                        fileStorageService,
                        attachmentMapper);

        // Set max file size to 10MB
        ReflectionTestUtils.setField(attachmentService, "maxFileSize", 10485760L);

        // Set up test user (employee)
        Role employeeRole = new Role();
        employeeRole.setId(UUID.randomUUID());
        employeeRole.setName(RoleType.EMPLOYEE);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setRole(employeeRole);

        // Set up finance user
        Role financeRole = new Role();
        financeRole.setId(UUID.randomUUID());
        financeRole.setName(RoleType.FINANCE);

        financeUser = new User();
        financeUser.setId(UUID.randomUUID());
        financeUser.setEmail("finance@example.com");
        financeUser.setName("Finance User");
        financeUser.setRole(financeRole);

        // Set up test expense
        testExpense = new Expense();
        testExpense.setId(UUID.randomUUID());
        testExpense.setCreatedBy(testUser);

        // Mock security context
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(testUser.getEmail());
    }

    @Test
    void uploadAttachment_WithValidFile_ShouldUploadSuccessfully() {
        // Arrange
        UUID expenseId = testExpense.getId();
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "test.pdf", "application/pdf", "test content".getBytes());

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByIdWithCreatedBy(expenseId))
                .thenReturn(Optional.of(testExpense));
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("stored/path/test.pdf");
        when(attachmentRepository.save(any(ExpenseAttachment.class)))
                .thenAnswer(
                        invocation -> {
                            ExpenseAttachment attachment = invocation.getArgument(0);
                            attachment.setId(UUID.randomUUID());
                            attachment.setUploadedAt(LocalDateTime.now());
                            return attachment;
                        });

        // Act
        AttachmentResponse response = attachmentService.uploadAttachment(expenseId, file);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getFilename()).isEqualTo("test.pdf");
        assertThat(response.getOriginalFilename()).isEqualTo("test.pdf");
        assertThat(response.getMimeType()).isEqualTo("application/pdf");
        assertThat(response.getFileSize()).isEqualTo("test content".getBytes().length);
        assertThat(response.getUploadedByEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getUploadedByName()).isEqualTo(testUser.getName());

        verify(fileStorageService).storeFile(file, expenseId.toString());
        verify(attachmentRepository).save(any(ExpenseAttachment.class));
    }

    @Test
    void uploadAttachment_WithEmptyFile_ShouldThrowException() {
        // Arrange
        UUID expenseId = UUID.randomUUID();
        MockMultipartFile emptyFile =
                new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.uploadAttachment(expenseId, emptyFile))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Cannot upload empty file");

        verify(fileStorageService, never()).storeFile(any(), any());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void uploadAttachment_WithOversizedFile_ShouldThrowException() {
        // Arrange
        UUID expenseId = UUID.randomUUID();
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile =
                new MockMultipartFile("file", "large.pdf", "application/pdf", largeContent);

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.uploadAttachment(expenseId, largeFile))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("File size exceeds maximum allowed size");

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    void uploadAttachment_WithInvalidMimeType_ShouldThrowException() {
        // Arrange
        UUID expenseId = UUID.randomUUID();
        MockMultipartFile invalidFile =
                new MockMultipartFile("file", "test.exe", "application/exe", "content".getBytes());

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.uploadAttachment(expenseId, invalidFile))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("File type not allowed");

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    void uploadAttachment_WithInvalidFilename_ShouldThrowException() {
        // Arrange
        UUID expenseId = UUID.randomUUID();
        MockMultipartFile invalidFile =
                new MockMultipartFile(
                        "file", "../../../malicious.pdf", "application/pdf", "content".getBytes());

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.uploadAttachment(expenseId, invalidFile))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Invalid filename");

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    void uploadAttachment_WithNonExistentExpense_ShouldThrowException() {
        // Arrange
        UUID nonExistentExpenseId = UUID.randomUUID();
        MockMultipartFile file =
                new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByIdWithCreatedBy(nonExistentExpenseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.uploadAttachment(nonExistentExpenseId, file))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense");

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    void uploadAttachment_WithUnauthorizedUser_ShouldThrowException() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setEmail("another@example.com");
        Role employeeRole = new Role();
        employeeRole.setName(RoleType.EMPLOYEE);
        anotherUser.setRole(employeeRole);

        UUID expenseId = testExpense.getId();
        MockMultipartFile file =
                new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        when(userRepository.findByEmail(anotherUser.getEmail()))
                .thenReturn(Optional.of(anotherUser));
        when(expenseRepository.findByIdWithCreatedBy(expenseId))
                .thenReturn(Optional.of(testExpense));
        when(authentication.getName()).thenReturn(anotherUser.getEmail());

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.uploadAttachment(expenseId, file))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not authorized");

        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    void uploadAttachment_WithFinanceUser_ShouldAllowUploadToAnyExpense() {
        // Arrange
        UUID expenseId = testExpense.getId();
        MockMultipartFile file =
                new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        when(authentication.getName()).thenReturn(financeUser.getEmail());
        when(userRepository.findByEmail(financeUser.getEmail()))
                .thenReturn(Optional.of(financeUser));
        when(expenseRepository.findByIdWithCreatedBy(expenseId))
                .thenReturn(Optional.of(testExpense));
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("stored/path/test.pdf");
        when(attachmentRepository.save(any(ExpenseAttachment.class)))
                .thenAnswer(
                        invocation -> {
                            ExpenseAttachment attachment = invocation.getArgument(0);
                            attachment.setId(UUID.randomUUID());
                            attachment.setUploadedAt(LocalDateTime.now());
                            return attachment;
                        });

        // Act
        AttachmentResponse response = attachmentService.uploadAttachment(expenseId, file);

        // Assert
        assertThat(response).isNotNull();
        verify(fileStorageService).storeFile(file, expenseId.toString());
    }

    @Test
    void getAttachmentsByExpense_WithValidExpense_ShouldReturnAttachments() {
        // Arrange
        UUID expenseId = testExpense.getId();

        ExpenseAttachment attachment1 = createTestAttachment(testExpense, testUser, "file1.pdf");
        ExpenseAttachment attachment2 = createTestAttachment(testExpense, testUser, "file2.pdf");
        List<ExpenseAttachment> attachments = Arrays.asList(attachment1, attachment2);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByIdWithCreatedBy(expenseId))
                .thenReturn(Optional.of(testExpense));
        when(attachmentRepository.findByExpenseIdWithUploadedBy(expenseId)).thenReturn(attachments);

        // Act
        List<AttachmentResponse> responses = attachmentService.getAttachmentsByExpense(expenseId);

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getFilename()).isEqualTo("file1.pdf");
        assertThat(responses.get(1).getFilename()).isEqualTo("file2.pdf");
    }

    @Test
    void getAttachmentsByExpense_WithNonExistentExpense_ShouldThrowException() {
        // Arrange
        UUID nonExistentExpenseId = UUID.randomUUID();

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByIdWithCreatedBy(nonExistentExpenseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.getAttachmentsByExpense(nonExistentExpenseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void downloadAttachment_WithValidAttachment_ShouldReturnResource() {
        // Arrange
        UUID attachmentId = UUID.randomUUID();
        ExpenseAttachment attachment = createTestAttachment(testExpense, testUser, "test.pdf");
        attachment.setId(attachmentId);
        attachment.setStoredPath("stored/path/test.pdf");

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(attachmentRepository.findByIdWithDetails(attachmentId))
                .thenReturn(Optional.of(attachment));
        when(fileStorageService.loadFileAsResource("stored/path/test.pdf"))
                .thenReturn(mockResource);

        // Act
        Resource resource = attachmentService.downloadAttachment(attachmentId);

        // Assert
        assertThat(resource).isNotNull();
        verify(fileStorageService).loadFileAsResource("stored/path/test.pdf");
    }

    @Test
    void downloadAttachment_WithNonExistentAttachment_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(attachmentRepository.findByIdWithDetails(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.downloadAttachment(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteAttachment_AsOwner_ShouldDeleteSuccessfully() {
        // Arrange
        UUID attachmentId = UUID.randomUUID();
        ExpenseAttachment attachment = createTestAttachment(testExpense, testUser, "test.pdf");
        attachment.setId(attachmentId);
        attachment.setStoredPath("stored/path/test.pdf");

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(attachmentRepository.findByIdWithDetails(attachmentId))
                .thenReturn(Optional.of(attachment));

        // Act
        attachmentService.deleteAttachment(attachmentId);

        // Assert
        verify(fileStorageService).deleteFile("stored/path/test.pdf");
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void deleteAttachment_AsFinanceUser_ShouldDeleteSuccessfully() {
        // Arrange
        UUID attachmentId = UUID.randomUUID();
        ExpenseAttachment attachment = createTestAttachment(testExpense, testUser, "test.pdf");
        attachment.setId(attachmentId);
        attachment.setStoredPath("stored/path/test.pdf");

        when(authentication.getName()).thenReturn(financeUser.getEmail());
        when(userRepository.findByEmail(financeUser.getEmail()))
                .thenReturn(Optional.of(financeUser));
        when(attachmentRepository.findByIdWithDetails(attachmentId))
                .thenReturn(Optional.of(attachment));

        // Act
        attachmentService.deleteAttachment(attachmentId);

        // Assert
        verify(fileStorageService).deleteFile("stored/path/test.pdf");
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void deleteAttachment_AsUnauthorizedUser_ShouldThrowException() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setEmail("another@example.com");
        Role employeeRole = new Role();
        employeeRole.setName(RoleType.EMPLOYEE);
        anotherUser.setRole(employeeRole);

        UUID attachmentId = UUID.randomUUID();
        ExpenseAttachment attachment = createTestAttachment(testExpense, testUser, "test.pdf");
        attachment.setId(attachmentId);

        when(authentication.getName()).thenReturn(anotherUser.getEmail());
        when(userRepository.findByEmail(anotherUser.getEmail()))
                .thenReturn(Optional.of(anotherUser));
        when(attachmentRepository.findByIdWithDetails(attachmentId))
                .thenReturn(Optional.of(attachment));

        // Act & Assert
        assertThatThrownBy(() -> attachmentService.deleteAttachment(attachmentId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not authorized");

        verify(fileStorageService, never()).deleteFile(any());
        verify(attachmentRepository, never()).delete(any());
    }

    @Test
    void uploadAttachment_WithJpegImage_ShouldUploadSuccessfully() {
        // Arrange
        UUID expenseId = testExpense.getId();
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "photo.jpg", "image/jpeg", "image content".getBytes());

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByIdWithCreatedBy(expenseId))
                .thenReturn(Optional.of(testExpense));
        when(fileStorageService.storeFile(any(), anyString())).thenReturn("stored/path/photo.jpg");
        when(attachmentRepository.save(any(ExpenseAttachment.class)))
                .thenAnswer(
                        invocation -> {
                            ExpenseAttachment attachment = invocation.getArgument(0);
                            attachment.setId(UUID.randomUUID());
                            attachment.setUploadedAt(LocalDateTime.now());
                            return attachment;
                        });

        // Act
        AttachmentResponse response = attachmentService.uploadAttachment(expenseId, file);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMimeType()).isEqualTo("image/jpeg");
    }

    @Test
    void uploadAttachment_WithPngImage_ShouldUploadSuccessfully() {
        // Arrange
        UUID expenseId = testExpense.getId();
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "screenshot.png", "image/png", "image content".getBytes());

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(expenseRepository.findByIdWithCreatedBy(expenseId))
                .thenReturn(Optional.of(testExpense));
        when(fileStorageService.storeFile(any(), anyString()))
                .thenReturn("stored/path/screenshot.png");
        when(attachmentRepository.save(any(ExpenseAttachment.class)))
                .thenAnswer(
                        invocation -> {
                            ExpenseAttachment attachment = invocation.getArgument(0);
                            attachment.setId(UUID.randomUUID());
                            attachment.setUploadedAt(LocalDateTime.now());
                            return attachment;
                        });

        // Act
        AttachmentResponse response = attachmentService.uploadAttachment(expenseId, file);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMimeType()).isEqualTo("image/png");
    }

    private ExpenseAttachment createTestAttachment(Expense expense, User user, String filename) {
        ExpenseAttachment attachment = new ExpenseAttachment();
        attachment.setId(UUID.randomUUID());
        attachment.setExpense(expense);
        attachment.setFilename(filename);
        attachment.setOriginalFilename(filename);
        attachment.setMimeType("application/pdf");
        attachment.setFileSize(1024L);
        attachment.setUploadedBy(user);
        attachment.setUploadedAt(LocalDateTime.now());
        return attachment;
    }
}
