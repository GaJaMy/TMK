package com.tmk.api.user.document.dto;

import com.tmk.api.user.document.result.DocumentUploadResult;
import com.tmk.core.document.entity.DocumentStatus;

public record DocumentUploadResponse(
        Long documentId,
        DocumentStatus status
) {

    public static DocumentUploadResponse from(DocumentUploadResult result) {
        return new DocumentUploadResponse(
                result.documentId(),
                result.status()
        );
    }
}
