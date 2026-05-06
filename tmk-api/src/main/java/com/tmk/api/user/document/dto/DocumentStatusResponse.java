package com.tmk.api.user.document.dto;

import com.tmk.api.user.document.result.DocumentStatusResult;
import com.tmk.core.document.entity.DocumentStatus;
import java.time.OffsetDateTime;

public record DocumentStatusResponse(
        Long documentId,
        String title,
        DocumentStatus status,
        int generatedQuestionCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static DocumentStatusResponse from(DocumentStatusResult result) {
        return new DocumentStatusResponse(
                result.documentId(),
                result.title(),
                result.status(),
                result.generatedQuestionCount(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
