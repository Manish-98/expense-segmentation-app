package com.expense.segmentation.service.storage;

import com.expense.segmentation.exception.BusinessException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path fileStorageLocation;

    public LocalFileStorageService(@Value("${file.upload-dir:uploads/expenses}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage location initialized at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory for file uploads", ex);
            throw new BusinessException(
                    "Could not create the directory where the uploaded files will be stored.");
        }
    }

    @Override
    public String storeFile(MultipartFile file, String expenseId) {
        // Sanitize filename
        String originalFilename =
                StringUtils.cleanPath(
                        file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");

        try {
            // Check if the filename contains invalid characters
            if (originalFilename.contains("..")) {
                throw new BusinessException(
                        "Filename contains invalid path sequence: " + originalFilename);
            }

            // Create expense-specific directory
            Path expenseDir = this.fileStorageLocation.resolve(expenseId);
            Files.createDirectories(expenseDir);

            // Generate unique filename with UUID prefix
            String fileExtension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = originalFilename.substring(dotIndex);
            }
            String uniqueFilename = UUID.randomUUID().toString() + "-" + originalFilename;

            // Store file
            Path targetLocation = expenseDir.resolve(uniqueFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return relative path from upload directory
            String storedPath = expenseId + "/" + uniqueFilename;
            log.info("File stored successfully: {}", storedPath);
            return storedPath;

        } catch (IOException ex) {
            log.error("Could not store file: {}", originalFilename, ex);
            throw new BusinessException("Could not store file: " + originalFilename);
        }
    }

    @Override
    public Resource loadFileAsResource(String storedPath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storedPath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.error("File not found or not readable: {}", storedPath);
                throw new BusinessException("File not found: " + storedPath);
            }
        } catch (MalformedURLException ex) {
            log.error("Malformed URL for file: {}", storedPath, ex);
            throw new BusinessException("File not found: " + storedPath);
        }
    }

    @Override
    public void deleteFile(String storedPath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storedPath).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted successfully: {}", storedPath);
        } catch (IOException ex) {
            log.error("Could not delete file: {}", storedPath, ex);
            throw new BusinessException("Could not delete file: " + storedPath);
        }
    }

    @Override
    public boolean fileExists(String storedPath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storedPath).normalize();
            return Files.exists(filePath);
        } catch (Exception ex) {
            log.error("Error checking file existence: {}", storedPath, ex);
            return false;
        }
    }

    @Override
    public Path getAbsolutePath(String storedPath) {
        return this.fileStorageLocation.resolve(storedPath).normalize();
    }
}
