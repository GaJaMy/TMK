package com.tmk.api.document;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileDocumentStorage {

    private final FileStorageProperties fileStorageProperties;

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        String originalFilename = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename()
                : "document.pdf";
        String sanitizedName = Path.of(originalFilename).getFileName().toString().replace(" ", "_");
        String storedName = UUID.randomUUID() + "-" + sanitizedName;
        Path storageDir = Path.of(fileStorageProperties.getStorageDir()).toAbsolutePath().normalize();
        Path target = storageDir.resolve(storedName);

        try {
            Files.createDirectories(storageDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }
}
