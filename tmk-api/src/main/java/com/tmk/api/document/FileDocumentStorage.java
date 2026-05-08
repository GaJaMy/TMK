package com.tmk.api.document;

import com.tmk.core.port.out.storage.FileStoragePort;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileDocumentStorage implements FileStoragePort {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public String store(String originalFilename, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        String normalizedOriginalFilename = StringUtils.hasText(originalFilename)
                ? originalFilename
                : "document.pdf";
        String sanitizedName = Path.of(normalizedOriginalFilename).getFileName().toString().replace(" ", "_");
        String storedName = UUID.randomUUID() + "-" + sanitizedName;
        Path storageDir = Path.of(fileStorageProperties.getStorageDir()).toAbsolutePath().normalize();
        Path target = storageDir.resolve(storedName);

        try {
            Files.createDirectories(storageDir);
            Files.write(target, bytes);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }

    @Override
    public void delete(String sourceReference) {
        if (!StringUtils.hasText(sourceReference)) {
            return;
        }

        Path target = Path.of(sourceReference).toAbsolutePath().normalize();
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }
}
