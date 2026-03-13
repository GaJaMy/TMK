package com.tmk.api.document.usecase;

import com.tmk.api.document.dto.DocumentStatusResult;
import com.tmk.api.document.dto.RegisterDocumentResult;
import com.tmk.core.document.entity.Document;
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
        Document document = registerDocumentService.register(title, source);
        return new RegisterDocumentResult(
                document.getId(),
                document.getTitle(),
                document.getStatus().name(),
                document.getCreatedAt().toString()
        );
    }

    public DocumentStatusResult getStatus(Long documentId) {
        Document document = getDocumentStatusService.getStatus(documentId);
        return new DocumentStatusResult(
                document.getId(),
                document.getTitle(),
                document.getStatus().name(),
                0, // chunk count: requires DocumentChunkPort - simplified for MVP
                0, // question count: requires QuestionPort - simplified for MVP
                document.getCreatedAt().toString()
        );
    }
}
