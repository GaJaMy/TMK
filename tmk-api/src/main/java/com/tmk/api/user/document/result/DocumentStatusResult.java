package com.tmk.api.user.document.result;

import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentStatus;
import java.time.OffsetDateTime;

public record DocumentStatusResult(
        Long documentId,
        String title,
        DocumentStatus status,
        int generatedQuestionCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static DocumentStatusResult from(Document document) {
        return new DocumentStatusResult(
                document.getId(),
                document.getTitle(),
                document.getStatus(),
                document.getGeneratedQuestionCount(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
