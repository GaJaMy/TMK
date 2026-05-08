package com.tmk.api.user.document.service;

import com.tmk.api.user.document.event.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DocumentUploadedEventHandler {

    private final AsyncDocumentQuestionGenerationService asyncDocumentQuestionGenerationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DocumentUploadedEvent event) {
        asyncDocumentQuestionGenerationService.processDocumentAsync(event.documentId());
    }
}
