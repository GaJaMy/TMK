package com.tmk.api.document.usecase;

import com.tmk.api.document.dto.DocumentStatusResult;
import com.tmk.api.document.dto.RegisterDocumentResult;
import com.tmk.core.document.service.GetDocumentStatusService;
import com.tmk.core.document.service.RegisterDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentUseCase {

    private final RegisterDocumentService registerDocumentService;
    private final GetDocumentStatusService getDocumentStatusService;

    public RegisterDocumentResult register(String title, String source) {
        registerDocumentService.register(title, source);
        // TODO: convert Document to RegisterDocumentResult
        return null;
    }

    public DocumentStatusResult getStatus(Long documentId) {
        getDocumentStatusService.getStatus(documentId);
        // TODO: convert Document to DocumentStatusResult
        return null;
    }
}
