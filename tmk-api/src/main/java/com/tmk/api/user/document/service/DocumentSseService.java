package com.tmk.api.user.document.service;

import com.tmk.api.user.document.result.DocumentStatusResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class DocumentSseService {

    private static final long SSE_TIMEOUT_MS = 30L * 60L * 1000L;
    private static final String DOCUMENT_STATUS_EVENT = "document-status";

    private final Map<String, List<SseEmitter>> emittersByKey = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId, DocumentStatusResult initialStatus) {
        String emitterKey = emitterKey(userId, initialStatus.documentId());
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emittersByKey.computeIfAbsent(emitterKey, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(emitterKey, emitter));
        emitter.onTimeout(() -> removeEmitter(emitterKey, emitter));
        emitter.onError(exception -> removeEmitter(emitterKey, emitter));

        send(emitterKey, emitter, initialStatus);
        return emitter;
    }

    public void publish(Long userId, DocumentStatusResult status) {
        String emitterKey = emitterKey(userId, status.documentId());
        List<SseEmitter> emitters = emittersByKey.getOrDefault(emitterKey, List.of());
        for (SseEmitter emitter : emitters) {
            send(emitterKey, emitter, status);
        }
    }

    private void send(String emitterKey, SseEmitter emitter, DocumentStatusResult status) {
        try {
            emitter.send(SseEmitter.event()
                    .name(DOCUMENT_STATUS_EVENT)
                    .data(status));
        } catch (IOException e) {
            removeEmitter(emitterKey, emitter);
        }
    }

    private void removeEmitter(String emitterKey, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByKey.get(emitterKey);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByKey.remove(emitterKey);
        }
    }

    private String emitterKey(Long userId, Long documentId) {
        return userId + ":" + documentId;
    }
}
