package com.tmk.api.user.document.result;

import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentStatus;

public record DocumentUploadResult(
        Long documentId,
        DocumentStatus status
) {

    public static DocumentUploadResult from(Document document) {
        return new DocumentUploadResult(
                document.getId(),
                document.getStatus()
        );
    }
}
