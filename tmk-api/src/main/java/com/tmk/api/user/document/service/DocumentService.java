package com.tmk.api.user.document.service;

import com.tmk.api.user.document.result.DocumentStatusResult;
import com.tmk.api.user.document.result.DocumentUploadResult;
import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentSourceType;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.DocumentPort;
import com.tmk.core.port.out.storage.FileStoragePort;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentPort documentPort;
    private final FileStoragePort fileStoragePort;

    @Transactional
    public DocumentUploadResult uploadDocument(
            Long userId,
            String title,
            String originalFilename,
            byte[] fileBytes
    ) {
        validateInput(title, originalFilename, fileBytes);

        DocumentSourceType sourceType = resolveSourceType(originalFilename);
        String sourceReference = fileStoragePort.store(originalFilename, fileBytes);
        OffsetDateTime now = OffsetDateTime.now();

        Document document = Document.create(
                userId,
                title.trim(),
                sourceType,
                sourceReference,
                now
        );

        Document savedDocument = documentPort.save(document);
        return DocumentUploadResult.from(savedDocument);
    }

    @Transactional(readOnly = true)
    public List<DocumentStatusResult> getDocuments(Long userId) {
        List<Document> documents = documentPort.findAllByUserIdOrderByCreatedAtDesc(userId);
        return documents.stream()
                .map(DocumentStatusResult::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentStatusResult getDocumentStatus(Long userId, Long documentId) {
        Document document = documentPort.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));
        return DocumentStatusResult.from(document);
    }

    private void validateInput(String title, String originalFilename, byte[] fileBytes) {
        if (!StringUtils.hasText(title) || !StringUtils.hasText(originalFilename) || fileBytes == null || fileBytes.length == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private DocumentSourceType resolveSourceType(String originalFilename) {
        String normalizedFilename = originalFilename.toLowerCase(Locale.ROOT);
        if (normalizedFilename.endsWith(".pdf")) {
            return DocumentSourceType.PDF_UPLOAD;
        }
        if (normalizedFilename.endsWith(".md")) {
            return DocumentSourceType.MD_UPLOAD;
        }
        throw new BusinessException(ErrorCode.UNSUPPORTED_DOCUMENT_SOURCE);
    }
}
