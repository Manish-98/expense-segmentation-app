package com.expense.segmentation.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.expense.segmentation.dto.CreateExpenseRequest;
import com.expense.segmentation.dto.RegisterRequest;
import com.expense.segmentation.model.ExpenseType;
import com.expense.segmentation.model.Role;
import com.expense.segmentation.model.RoleType;
import com.expense.segmentation.repository.ExpenseAttachmentRepository;
import com.expense.segmentation.repository.ExpenseRepository;
import com.expense.segmentation.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ExpenseAttachmentIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private RoleRepository roleRepository;

    @Autowired private ExpenseRepository expenseRepository;

    @Autowired private ExpenseAttachmentRepository attachmentRepository;

    @Autowired private com.expense.segmentation.repository.UserRepository userRepository;

    private String employeeToken;
    private String financeToken;
    private UUID expenseId;
    private Path testUploadDir;

    @BeforeEach
    void setUp() throws Exception {
        // Create test upload directory
        testUploadDir = Files.createTempDirectory("test-uploads-integration");

        // Ensure roles exist
        ensureRoleExists(RoleType.EMPLOYEE);
        ensureRoleExists(RoleType.FINANCE);

        // Register and login as employee
        employeeToken = registerAndGetToken("Employee User", "employee@test.com", "password123");

        // Register and login as finance user
        financeToken = registerAndGetToken("Finance User", "finance@test.com", "password123");

        // Manually update finance user's role
        updateUserRole("finance@test.com", RoleType.FINANCE);

        // Create a test expense
        expenseId = createTestExpense(employeeToken);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test directory
        if (Files.exists(testUploadDir)) {
            Files.walk(testUploadDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(
                            path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    // Ignore cleanup errors
                                }
                            });
        }
    }

    @Test
    void fullAttachmentFlow_UploadListDownloadDelete_ShouldSucceed() throws Exception {
        // Step 1: Upload attachment
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "receipt.pdf", "application/pdf", "receipt content".getBytes());

        MvcResult uploadResult =
                mockMvc.perform(
                                multipart("/expenses/" + expenseId + "/attachments")
                                        .file(file)
                                        .header("Authorization", "Bearer " + employeeToken))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(notNullValue()))
                        .andExpect(jsonPath("$.expenseId").value(expenseId.toString()))
                        .andExpect(jsonPath("$.filename").value(notNullValue()))
                        .andExpect(jsonPath("$.originalFilename").value("receipt.pdf"))
                        .andExpect(jsonPath("$.mimeType").value("application/pdf"))
                        .andExpect(
                                jsonPath("$.fileSize").value("receipt content".getBytes().length))
                        .andExpect(jsonPath("$.uploadedByEmail").value("employee@test.com"))
                        .andExpect(jsonPath("$.uploadedByName").value("Employee User"))
                        .andReturn();

        String uploadResponseBody = uploadResult.getResponse().getContentAsString();
        String attachmentId = objectMapper.readTree(uploadResponseBody).get("id").asText();

        // Step 2: List attachments
        mockMvc.perform(
                        get("/expenses/" + expenseId + "/attachments")
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(attachmentId))
                .andExpect(jsonPath("$[0].originalFilename").value("receipt.pdf"));

        // Step 3: Download attachment
        mockMvc.perform(
                        get("/expenses/" + expenseId + "/attachments/" + attachmentId)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(
                        header().string(
                                        "Content-Disposition",
                                        "attachment; filename=\"receipt.pdf\""));

        // Step 4: Delete attachment
        mockMvc.perform(
                        delete("/expenses/" + expenseId + "/attachments/" + attachmentId)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNoContent());

        // Step 5: Verify attachment is deleted
        mockMvc.perform(
                        get("/expenses/" + expenseId + "/attachments")
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void uploadMultipleAttachments_ToSameExpense_ShouldSucceed() throws Exception {
        // Upload first attachment
        MockMultipartFile file1 =
                new MockMultipartFile(
                        "file", "receipt1.pdf", "application/pdf", "content1".getBytes());

        mockMvc.perform(
                        multipart("/expenses/" + expenseId + "/attachments")
                                .file(file1)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isCreated());

        // Upload second attachment
        MockMultipartFile file2 =
                new MockMultipartFile(
                        "file", "receipt2.pdf", "application/pdf", "content2".getBytes());

        mockMvc.perform(
                        multipart("/expenses/" + expenseId + "/attachments")
                                .file(file2)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isCreated());

        // Verify both attachments are listed
        mockMvc.perform(
                        get("/expenses/" + expenseId + "/attachments")
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void uploadAttachment_WithDifferentFileTypes_ShouldSucceed() throws Exception {
        // Upload PDF
        MockMultipartFile pdfFile =
                new MockMultipartFile(
                        "file", "document.pdf", "application/pdf", "pdf content".getBytes());

        mockMvc.perform(
                        multipart("/expenses/" + expenseId + "/attachments")
                                .file(pdfFile)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mimeType").value("application/pdf"));

        // Upload JPEG
        MockMultipartFile jpegFile =
                new MockMultipartFile("file", "photo.jpg", "image/jpeg", "jpeg content".getBytes());

        mockMvc.perform(
                        multipart("/expenses/" + expenseId + "/attachments")
                                .file(jpegFile)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mimeType").value("image/jpeg"));

        // Upload PNG
        MockMultipartFile pngFile =
                new MockMultipartFile(
                        "file", "screenshot.png", "image/png", "png content".getBytes());

        mockMvc.perform(
                        multipart("/expenses/" + expenseId + "/attachments")
                                .file(pngFile)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mimeType").value("image/png"));

        // Verify all three attachments
        mockMvc.perform(
                        get("/expenses/" + expenseId + "/attachments")
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void uploadAttachment_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile emptyFile =
                new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(
                        multipart("/expenses/" + expenseId + "/attachments")
                                .file(emptyFile)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAttachment_WithInvalidMimeType_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile invalidFile =
                new MockMultipartFile(
                        "file", "malware.exe", "application/exe", "malicious content".getBytes());

        mockMvc.perform(
                        multipart("/expenses/" + expenseId + "/attachments")
                                .file(invalidFile)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void financeUser_CanAccessAllExpenseAttachments() throws Exception {
        // Employee uploads attachment
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "receipt.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(
                        multipart("/expenses/" + expenseId + "/attachments")
                                .file(file)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isCreated());

        // Finance user can list attachments
        mockMvc.perform(
                        get("/expenses/" + expenseId + "/attachments")
                                .header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void financeUser_CanDeleteAnyAttachment() throws Exception {
        // Employee uploads attachment
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "receipt.pdf", "application/pdf", "content".getBytes());

        MvcResult uploadResult =
                mockMvc.perform(
                                multipart("/expenses/" + expenseId + "/attachments")
                                        .file(file)
                                        .header("Authorization", "Bearer " + employeeToken))
                        .andExpect(status().isCreated())
                        .andReturn();

        String uploadResponseBody = uploadResult.getResponse().getContentAsString();
        String attachmentId = objectMapper.readTree(uploadResponseBody).get("id").asText();

        // Finance user can delete the attachment
        mockMvc.perform(
                        delete("/expenses/" + expenseId + "/attachments/" + attachmentId)
                                .header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void accessAttachment_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/expenses/" + expenseId + "/attachments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadAttachment_ToNonExistentExpense_ShouldReturnNotFound() throws Exception {
        UUID nonExistentExpenseId = UUID.randomUUID();
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "receipt.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(
                        multipart("/expenses/" + nonExistentExpenseId + "/attachments")
                                .file(file)
                                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNotFound());
    }

    // Helper methods

    private void ensureRoleExists(RoleType roleType) {
        if (!roleRepository.existsByName(roleType)) {
            Role role = new Role();
            role.setName(roleType);
            role.setDescription(roleType.name() + " role");
            roleRepository.save(role);
        }
    }

    private String registerAndGetToken(String name, String email, String password)
            throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(name, email, password);

        MvcResult result =
                mockMvc.perform(
                                post("/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(registerRequest)))
                        .andExpect(status().isCreated())
                        .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    private void updateUserRole(String email, RoleType roleType) {
        userRepository
                .findByEmail(email)
                .ifPresent(
                        user -> {
                            roleRepository
                                    .findByName(roleType)
                                    .ifPresent(
                                            role -> {
                                                user.setRole(role);
                                                userRepository.save(user);
                                            });
                        });
    }

    private UUID createTestExpense(String token) throws Exception {
        CreateExpenseRequest expenseRequest = new CreateExpenseRequest();
        expenseRequest.setDate(LocalDate.now());
        expenseRequest.setVendor("Test Vendor");
        expenseRequest.setAmount(new BigDecimal("100.00"));
        expenseRequest.setDescription("Test expense for attachments");
        expenseRequest.setType(ExpenseType.EXPENSE);

        MvcResult result =
                mockMvc.perform(
                                post("/expenses")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(expenseRequest)))
                        .andExpect(status().isCreated())
                        .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }
}
