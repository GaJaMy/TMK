package com.tmk.api.document;

import com.tmk.api.document.dto.DocumentStatusResult;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DocumentSseService {

    private static final long TIMEOUT_MS = 30L * 60L * 1000L;
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long documentId, DocumentStatusResult initialStatus) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.computeIfAbsent(documentId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(documentId, emitter));
        emitter.onTimeout(() -> remove(documentId, emitter));
        emitter.onError(exception -> remove(documentId, emitter));

        send(emitter, "connected", initialStatus);
        return emitter;
    }

    public void publish(DocumentStatusResult status) {
        List<SseEmitter> documentEmitters = emitters.get(status.getDocumentId());
        if (documentEmitters == null || documentEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : documentEmitters) {
            if (!send(emitter, "document-status", status)) {
                remove(status.getDocumentId(), emitter);
            }
        }

        if ("COMPLETED".equals(status.getStatus()) || "FAILED".equals(status.getStatus())) {
            complete(status.getDocumentId());
        }
    }

    private boolean send(SseEmitter emitter, String eventName, DocumentStatusResult payload) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(payload));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void complete(Long documentId) {
        List<SseEmitter> documentEmitters = emitters.remove(documentId);
        if (documentEmitters == null) {
            return;
        }
        for (SseEmitter emitter : documentEmitters) {
            emitter.complete();
        }
    }

    private void remove(Long documentId, SseEmitter emitter) {
        List<SseEmitter> documentEmitters = emitters.get(documentId);
        if (documentEmitters == null) {
            return;
        }
        documentEmitters.remove(emitter);
        if (documentEmitters.isEmpty()) {
            emitters.remove(documentId);
        }
    }
}
