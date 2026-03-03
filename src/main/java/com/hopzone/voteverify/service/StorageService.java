package com.hopzone.voteverify.service;

import com.hopzone.voteverify.config.AppProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private static final long MAX_FILE_SIZE = 8 * 1024 * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp");

    private final Path storageRoot;

    public StorageService(AppProperties appProperties) {
        this.storageRoot = Path.of(appProperties.getStorageDir()).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(storageRoot);
    }

    public String store(MultipartFile file, String label) throws IOException {
        validate(file, label);
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = storageRoot.resolve(filename).normalize();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return "/uploads/" + filename;
    }

    public void deleteIfExists(String publicPath) {
        if (!StringUtils.hasText(publicPath) || !publicPath.startsWith("/uploads/")) {
            return;
        }
        String filename = publicPath.substring("/uploads/".length());
        Path target = storageRoot.resolve(filename).normalize();
        if (!target.startsWith(storageRoot)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // Best effort cleanup.
        }
    }

    private void validate(MultipartFile file, String label) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(label + " exceeds 8MB.");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(label + " must be png/jpg/jpeg/webp.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(label + " has unsupported MIME type.");
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
