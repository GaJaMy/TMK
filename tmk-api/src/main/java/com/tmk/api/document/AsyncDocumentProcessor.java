package com.tmk.api.document;

import com.tmk.api.document.dto.DocumentStatusResult;
import com.tmk.core.document.service.GetDocumentStatusService;
import com.tmk.core.document.service.DocumentProcessingService;
import com.tmk.core.document.vo.DocumentStatusInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncDocumentProcessor {

    private final DocumentProcessingService documentProcessingService;
    private final GetDocumentStatusService getDocumentStatusService;
    private final DocumentSseService documentSseService;

    @Async
    public void processAsync(Long documentId) {
        log.info("Starting async document processing for documentId={}", documentId);
        try {
            documentProcessingService.process(documentId);
            log.info("Document processing completed for documentId={}", documentId);
        } catch (Exception e) {
            log.error("Document processing failed for documentId={}", documentId, e);
        } finally {
            try {
                DocumentStatusInfo info = getDocumentStatusService.getStatus(documentId);
                documentSseService.publish(new DocumentStatusResult(
                        info.document().getId(),
                        info.document().getTitle(),
                        info.document().getTopic().name(),
                        info.document().getScope().name(),
                        info.document().getStatus().name(),
                        (int) info.chunkCount(),
                        (int) info.questionCount(),
                        info.document().getCreatedAt().toString()
                ));
            } catch (Exception publishException) {
                log.warn("Failed to publish document SSE status for documentId={}", documentId, publishException);
            }
        }
    }
}
