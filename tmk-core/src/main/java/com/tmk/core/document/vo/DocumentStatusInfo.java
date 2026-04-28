package com.tmk.core.document.vo;

import com.tmk.core.document.entity.DocumentStatus;
import java.time.OffsetDateTime;

public record DocumentStatusInfo(
        Long documentId,
        String title,
        DocumentStatus status,
        int generatedQuestionCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
