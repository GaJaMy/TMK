package com.tmk.api.document;

import com.tmk.core.document.service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncDocumentProcessor {

    private final DocumentProcessingService documentProcessingService;

    @Async
    public void processAsync(Long documentId) {
        log.info("Starting async document processing for documentId={}", documentId);
        try {
            documentProcessingService.process(documentId);
            log.info("Document processing completed for documentId={}", documentId);
        } catch (Exception e) {
            log.error("Document processing failed for documentId={}", documentId, e);
        }
    }
}
