package com.tmk.api.document.usecase;

import com.tmk.api.document.AsyncDocumentProcessor;
import com.tmk.api.document.DocumentSseService;
import com.tmk.api.document.dto.DocumentStatusResult;
import com.tmk.api.document.dto.RegisterDocumentResult;
import com.tmk.api.security.jwt.JwtProvider;
import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.document.entity.Document;
import com.tmk.core.document.service.GetDocumentStatusService;
import com.tmk.core.document.service.RegisterDocumentService;
import com.tmk.core.document.vo.DocumentStatusInfo;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class DocumentUseCase {

    private final RegisterDocumentService registerDocumentService;
    private final GetDocumentStatusService getDocumentStatusService;
    private final AsyncDocumentProcessor asyncDocumentProcessor;
    private final DocumentSseService documentSseService;
    private final JwtProvider jwtProvider;

    public RegisterDocumentResult registerPrivate(String title, String source, String topic, Long ownerUserId) {
        Topic documentTopic = Topic.valueOf(topic.toUpperCase());
        Document document = registerDocumentService.register(title, source, documentTopic, ContentScope.PRIVATE, ownerUserId);
        asyncDocumentProcessor.processAsync(document.getId());
        return new RegisterDocumentResult(
                document.getId(),
                document.getTitle(),
                document.getTopic().name(),
                document.getScope().name(),
                document.getStatus().name(),
                document.getCreatedAt().toString()
        );
    }

    public RegisterDocumentResult registerPublic(String title, String source, String topic) {
        Topic documentTopic = Topic.valueOf(topic.toUpperCase());
        Document document = registerDocumentService.register(title, source, documentTopic, ContentScope.PUBLIC, null);
        asyncDocumentProcessor.processAsync(document.getId());
        return new RegisterDocumentResult(
                document.getId(),
                document.getTitle(),
                document.getTopic().name(),
                document.getScope().name(),
                document.getStatus().name(),
                document.getCreatedAt().toString()
        );
    }

    public DocumentStatusResult getPrivateStatus(Long documentId, Long ownerUserId) {
        Document document = getDocumentStatusService.getStatus(documentId).document();
        if (document.getScope() != ContentScope.PRIVATE || !ownerUserId.equals(document.getOwnerUserId())) {
            throw new com.tmk.core.exception.BusinessException(com.tmk.core.exception.ErrorCode.FORBIDDEN);
        }
        return mapStatus(documentId);
    }

    public DocumentStatusResult getStatus(Long documentId) {
        return mapStatus(documentId);
    }

    public SseEmitter subscribePrivate(Long documentId, String accessToken) {
        ClaimsView claims = validateAccessToken(accessToken);
        Document document = getDocumentStatusService.getStatus(documentId).document();
        if (document.getScope() != ContentScope.PRIVATE || !claims.userId().equals(document.getOwnerUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return documentSseService.subscribe(documentId, mapStatus(documentId));
    }

    public SseEmitter subscribeAdmin(Long documentId, String accessToken) {
        ClaimsView claims = validateAccessToken(accessToken);
        if (!"ADMIN".equals(claims.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return documentSseService.subscribe(documentId, mapStatus(documentId));
    }

    public void publishStatus(Long documentId) {
        documentSseService.publish(mapStatus(documentId));
    }

    private DocumentStatusResult mapStatus(Long documentId) {
        DocumentStatusInfo info = getDocumentStatusService.getStatus(documentId);
        Document document = info.document();
        return new DocumentStatusResult(
                document.getId(),
                document.getTitle(),
                document.getTopic().name(),
                document.getScope().name(),
                document.getStatus().name(),
                (int) info.chunkCount(),
                (int) info.questionCount(),
                document.getCreatedAt().toString()
        );
    }

    private ClaimsView validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank() || !jwtProvider.validateToken(accessToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        var claims = jwtProvider.parseClaims(accessToken);
        return new ClaimsView(claims.get("userId", Long.class), claims.get("role", String.class));
    }

    private record ClaimsView(Long userId, String role) {
    }
}
