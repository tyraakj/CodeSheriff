package com.backend.CodeSheriff.Validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
public class ZipFileValidator {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final long MAX_UNCOMPRESSED_SIZE = 500 * 1024 * 1024; // 500MB
    private static final int MAX_ENTRIES = 10000;

    public void validate(MultipartFile zipFile) throws IOException {
        // 1. Check if file is empty
        if (zipFile.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // 2. Validate file type
        String contentType = zipFile.getContentType();
        if (!"application/zip".equals(contentType) &&
            !"application/x-zip-compressed".equals(contentType) &&
            !"application/octet-stream".equals(contentType)) {
            throw new IllegalArgumentException("Only ZIP files are allowed. Received: " + contentType);
        }

        // 3. Validate file size
        if (zipFile.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // 4. Validate ZIP structure and contents
        validateZipStructure(zipFile);
    }

    private void validateZipStructure(MultipartFile zipFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            int entryCount = 0;
            long totalUncompressedSize = 0;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                // Check for path traversal attacks
                if (name.contains("..") || name.startsWith("/") || name.contains("\\..")) {
                    throw new SecurityException("Invalid file path detected: " + name);
                }

                // Check for absolute paths
                if (name.startsWith("/") || (name.length() > 1 && name.charAt(1) == ':')) {
                    throw new SecurityException("Absolute paths are not allowed: " + name);
                }

                // Count entries to prevent zip bombs
                entryCount++;
                if (entryCount > MAX_ENTRIES) {
                    throw new SecurityException(
                        String.format("ZIP file contains too many entries (max: %d)", MAX_ENTRIES)
                    );
                }

                // Check uncompressed size to prevent zip bombs
                long size = entry.getSize();
                if (size > 0) {
                    totalUncompressedSize += size;
                    if (totalUncompressedSize > MAX_UNCOMPRESSED_SIZE) {
                        throw new SecurityException(
                            String.format("Uncompressed size exceeds maximum allowed (%d MB)",
                                MAX_UNCOMPRESSED_SIZE / (1024 * 1024))
                        );
                    }
                }

                // Validate that only .java files are included (optional, can be relaxed)
                if (!entry.isDirectory() && !name.endsWith(".java")) {
                    log.warn("Non-Java file found in ZIP: {}", name);
                }

                zis.closeEntry();
            }

            log.info("ZIP validation passed: {} entries, {} bytes uncompressed",
                entryCount, totalUncompressedSize);
        }
    }
}

// Made with Bob
