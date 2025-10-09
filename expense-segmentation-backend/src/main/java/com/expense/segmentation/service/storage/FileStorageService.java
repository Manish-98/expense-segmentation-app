package com.expense.segmentation.service.storage;

import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for file storage operations. This abstraction allows for pluggable storage
 * implementations (local file system, S3, etc.)
 */
public interface FileStorageService {

    /**
     * Store a file and return the stored path
     *
     * @param file the file to store
     * @param expenseId the expense ID for organizing files
     * @return the stored file path
     */
    String storeFile(MultipartFile file, String expenseId);

    /**
     * Load a file as a Resource
     *
     * @param storedPath the stored file path
     * @return the file as a Resource
     */
    Resource loadFileAsResource(String storedPath);

    /**
     * Delete a file
     *
     * @param storedPath the stored file path
     */
    void deleteFile(String storedPath);

    /**
     * Check if a file exists
     *
     * @param storedPath the stored file path
     * @return true if the file exists, false otherwise
     */
    boolean fileExists(String storedPath);

    /**
     * Get the absolute path of a stored file
     *
     * @param storedPath the stored file path
     * @return the absolute path
     */
    Path getAbsolutePath(String storedPath);
}
