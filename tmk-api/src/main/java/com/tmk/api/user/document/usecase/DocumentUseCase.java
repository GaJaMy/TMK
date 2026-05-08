package com.tmk.api.user.document.usecase;

import com.tmk.api.monitoring.event.DocumentRegisteredEvent;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.security.jwt.JwtProvider;
import com.tmk.api.user.document.dto.DocumentStatusResponse;
import com.tmk.api.user.document.dto.DocumentUploadResponse;
import com.tmk.api.user.document.request.DocumentUploadRequest;
import com.tmk.api.user.document.result.DocumentStatusResult;
import com.tmk.api.user.document.result.DocumentUploadResult;
import com.tmk.api.user.document.service.AsyncDocumentQuestionGenerationService;
import com.tmk.api.user.document.service.DocumentService;
import com.tmk.api.user.document.service.DocumentSseService;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.cache.TokenBlacklistPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class DocumentUseCase {

    private final DocumentService documentService;
    private final AsyncDocumentQuestionGenerationService asyncDocumentQuestionGenerationService;
    private final DocumentSseService documentSseService;
    private final JwtProvider jwtProvider;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public DocumentUploadResponse uploadDocument(Long userId, DocumentUploadRequest request) {
        MultipartFile file = request.getFile();
        byte[] fileBytes = readBytes(file);

        DocumentUploadResult result = documentService.uploadDocument(
                userId,
                request.getTitle(),
                file.getOriginalFilename(),
                fileBytes
        );
        applicationEventPublisher.publishEvent(new DocumentRegisteredEvent(userId));
        asyncDocumentQuestionGenerationService.processDocumentAsync(result.documentId());
        return DocumentUploadResponse.from(result);
    }

    @Transactional(readOnly = true)
    public List<DocumentStatusResponse> getDocuments(Long userId) {
        List<DocumentStatusResult> results = documentService.getDocuments(userId);
        return results.stream()
                .map(DocumentStatusResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentStatusResponse getDocumentStatus(Long userId, Long documentId) {
        DocumentStatusResult result = documentService.getDocumentStatus(userId, documentId);
        return DocumentStatusResponse.from(result);
    }

    @Transactional(readOnly = true)
    public SseEmitter subscribeDocumentEvents(Long documentId, String accessToken) {
        Long userId = extractUserId(accessToken);
        DocumentStatusResult result = documentService.getDocumentStatus(userId, documentId);
        return documentSseService.subscribe(userId, result);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }

    private Long extractUserId(String accessToken) {
        if (tokenBlacklistPort.isBlacklisted(accessToken)) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        try {
            Claims claims = jwtProvider.parseClaims(accessToken);
            String principalType = claims.get("principalType", String.class);
            if (!AuthenticatedPrincipal.USER_PRINCIPAL_TYPE.equals(principalType)) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            return claims.get("principalId", Long.class);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }
}
