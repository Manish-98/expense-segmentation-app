package com.expense.segmentation.service.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.expense.segmentation.exception.InvalidOperationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

class LocalFileStorageServiceTest {

    private LocalFileStorageService fileStorageService;
    private Path testUploadDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary directory for testing
        testUploadDir = Files.createTempDirectory("test-uploads");
        fileStorageService = new LocalFileStorageService(testUploadDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test directory
        if (Files.exists(testUploadDir)) {
            Files.walk(testUploadDir)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
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
    void storeFile_WithValidFile_ShouldStoreFileSuccessfully() throws IOException {
        // Arrange
        String expenseId = "test-expense-123";
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "test.pdf", "application/pdf", "test content".getBytes());

        // Act
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Assert
        assertThat(storedPath).isNotNull();
        assertThat(storedPath).startsWith(expenseId + "/");
        assertThat(storedPath).contains("test.pdf");

        // Verify file exists
        Path fullPath = testUploadDir.resolve(storedPath);
        assertThat(Files.exists(fullPath)).isTrue();
        assertThat(Files.readString(fullPath)).isEqualTo("test content");
    }

    @Test
    void storeFile_WithInvalidPathCharacters_ShouldThrowException() {
        // Arrange
        String expenseId = "test-expense-123";
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "../../../malicious.pdf", "application/pdf", "content".getBytes());

        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.storeFile(file, expenseId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("invalid path sequence");
    }

    @Test
    void storeFile_CreatesExpenseDirectory_WhenNotExists() throws IOException {
        // Arrange
        String expenseId = "new-expense-456";
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "document.pdf", "application/pdf", "content".getBytes());

        // Act
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Assert
        Path expenseDir = testUploadDir.resolve(expenseId);
        assertThat(Files.exists(expenseDir)).isTrue();
        assertThat(Files.isDirectory(expenseDir)).isTrue();
    }

    @Test
    void storeFile_WithFileExtension_ShouldPreserveExtension() throws IOException {
        // Arrange
        String expenseId = "test-expense";
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "report.pdf", "application/pdf", "pdf content".getBytes());

        // Act
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Assert
        assertThat(storedPath).endsWith(".pdf");
    }

    @Test
    void loadFileAsResource_WithExistingFile_ShouldReturnResource() throws IOException {
        // Arrange
        String expenseId = "test-expense";
        MockMultipartFile file =
                new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Act
        Resource resource = fileStorageService.loadFileAsResource(storedPath);

        // Assert
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    void loadFileAsResource_WithNonExistingFile_ShouldThrowException() {
        // Arrange
        String nonExistentPath = "non-existent/file.pdf";

        // Act & Assert
        assertThatThrownBy(() -> fileStorageService.loadFileAsResource(nonExistentPath))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    void deleteFile_WithExistingFile_ShouldDeleteSuccessfully() throws IOException {
        // Arrange
        String expenseId = "test-expense";
        MockMultipartFile file =
                new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Verify file exists before deletion
        assertThat(fileStorageService.fileExists(storedPath)).isTrue();

        // Act
        fileStorageService.deleteFile(storedPath);

        // Assert
        assertThat(fileStorageService.fileExists(storedPath)).isFalse();
    }

    @Test
    void deleteFile_WithNonExistingFile_ShouldNotThrowException() {
        // Arrange
        String nonExistentPath = "non-existent/file.pdf";

        // Act & Assert - should not throw exception
        fileStorageService.deleteFile(nonExistentPath);
    }

    @Test
    void fileExists_WithExistingFile_ShouldReturnTrue() throws IOException {
        // Arrange
        String expenseId = "test-expense";
        MockMultipartFile file =
                new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Act
        boolean exists = fileStorageService.fileExists(storedPath);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void fileExists_WithNonExistingFile_ShouldReturnFalse() {
        // Arrange
        String nonExistentPath = "non-existent/file.pdf";

        // Act
        boolean exists = fileStorageService.fileExists(nonExistentPath);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void getAbsolutePath_ShouldReturnCorrectPath() throws IOException {
        // Arrange
        String expenseId = "test-expense";
        MockMultipartFile file =
                new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Act
        Path absolutePath = fileStorageService.getAbsolutePath(storedPath);

        // Assert
        assertThat(absolutePath).isNotNull();
        assertThat(absolutePath.isAbsolute()).isTrue();
        assertThat(Files.exists(absolutePath)).isTrue();
    }

    @Test
    void storeFile_WithNullFilename_ShouldUseDefaultFilename() throws IOException {
        // Arrange
        String expenseId = "test-expense";
        MockMultipartFile file =
                new MockMultipartFile("file", null, "application/pdf", "content".getBytes());

        // Act
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Assert
        assertThat(storedPath).isNotNull();
        assertThat(storedPath).startsWith(expenseId + "/");
    }

    @Test
    void storeFile_MultipleFilesInSameExpense_ShouldCreateUniqueFilenames() throws IOException {
        // Arrange
        String expenseId = "test-expense";
        MockMultipartFile file1 =
                new MockMultipartFile("file", "test.pdf", "application/pdf", "content1".getBytes());
        MockMultipartFile file2 =
                new MockMultipartFile("file", "test.pdf", "application/pdf", "content2".getBytes());

        // Act
        String storedPath1 = fileStorageService.storeFile(file1, expenseId);
        String storedPath2 = fileStorageService.storeFile(file2, expenseId);

        // Assert
        assertThat(storedPath1).isNotEqualTo(storedPath2);
        assertThat(fileStorageService.fileExists(storedPath1)).isTrue();
        assertThat(fileStorageService.fileExists(storedPath2)).isTrue();
    }

    @Test
    void storeFile_WithFileWithoutExtension_ShouldStoreSuccessfully() throws IOException {
        // Arrange
        String expenseId = "test-expense";
        MockMultipartFile file =
                new MockMultipartFile("file", "README", "text/plain", "readme content".getBytes());

        // Act
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Assert
        assertThat(storedPath).isNotNull();
        assertThat(storedPath).startsWith(expenseId + "/");
        assertThat(storedPath).contains("README");
        assertThat(fileStorageService.fileExists(storedPath)).isTrue();
    }

    @Test
    void loadFileAsResource_WithUnreadableFile_ShouldThrowException() throws IOException {
        // Arrange
        String expenseId = "test-expense";
        MockMultipartFile file =
                new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Make file unreadable
        Path filePath = testUploadDir.resolve(storedPath);
        filePath.toFile().setReadable(false);

        try {
            // Act & Assert
            assertThatThrownBy(() -> fileStorageService.loadFileAsResource(storedPath))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("File not found");
        } finally {
            // Restore permissions for cleanup
            filePath.toFile().setReadable(true);
        }
    }

    @Test
    void storeFile_WithEmptyFilename_ShouldUseDefault() throws IOException {
        // Arrange
        String expenseId = "test-expense";
        MockMultipartFile file =
                new MockMultipartFile("file", "", "application/pdf", "content".getBytes());

        // Act
        String storedPath = fileStorageService.storeFile(file, expenseId);

        // Assert
        assertThat(storedPath).isNotNull();
        assertThat(storedPath).startsWith(expenseId + "/");
    }

    @Test
    void getAbsolutePath_WithRelativePath_ShouldReturnAbsolutePath() {
        // Arrange
        String relativePath = "expense-123/test.pdf";

        // Act
        Path absolutePath = fileStorageService.getAbsolutePath(relativePath);

        // Assert
        assertThat(absolutePath).isNotNull();
        assertThat(absolutePath.isAbsolute()).isTrue();
        assertThat(absolutePath.toString()).contains("expense-123");
        assertThat(absolutePath.toString()).contains("test.pdf");
    }

    @Test
    void storeFile_WithDifferentFileTypes_ShouldStoreCorrectly() throws IOException {
        // Arrange
        String expenseId = "test-expense";

        // Test with image
        MockMultipartFile imageFile =
                new MockMultipartFile("file", "image.jpg", "image/jpeg", "image".getBytes());
        String imagePath = fileStorageService.storeFile(imageFile, expenseId);

        // Test with document
        MockMultipartFile docFile =
                new MockMultipartFile("file", "doc.pdf", "application/pdf", "doc".getBytes());
        String docPath = fileStorageService.storeFile(docFile, expenseId);

        // Assert
        assertThat(imagePath).endsWith(".jpg");
        assertThat(docPath).endsWith(".pdf");
        assertThat(fileStorageService.fileExists(imagePath)).isTrue();
        assertThat(fileStorageService.fileExists(docPath)).isTrue();
    }
}
