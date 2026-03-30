package com.tmk.api.document.usecase;

import com.tmk.api.document.AsyncDocumentProcessor;
import com.tmk.api.document.dto.DocumentStatusResult;
import com.tmk.api.document.dto.RegisterDocumentResult;
import com.tmk.core.document.entity.Document;
import com.tmk.core.document.service.GetDocumentStatusService;
import com.tmk.core.document.service.RegisterDocumentService;
import com.tmk.core.document.vo.DocumentStatusInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentUseCase {

    private final RegisterDocumentService registerDocumentService;
    private final GetDocumentStatusService getDocumentStatusService;
    private final AsyncDocumentProcessor asyncDocumentProcessor;

    public RegisterDocumentResult register(String title, String source) {
        Document document = registerDocumentService.register(title, source);
        asyncDocumentProcessor.processAsync(document.getId());
        return new RegisterDocumentResult(
                document.getId(),
                document.getTitle(),
                document.getStatus().name(),
                document.getCreatedAt().toString()
        );
    }

    public DocumentStatusResult getStatus(Long documentId) {
        DocumentStatusInfo info = getDocumentStatusService.getStatus(documentId);
        Document document = info.document();
        return new DocumentStatusResult(
                document.getId(),
                document.getTitle(),
                document.getStatus().name(),
                (int) info.chunkCount(),
                (int) info.questionCount(),
                document.getCreatedAt().toString()
        );
    }
}
