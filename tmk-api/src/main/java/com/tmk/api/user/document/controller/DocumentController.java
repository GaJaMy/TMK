package com.tmk.api.user.document.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.user.document.dto.DocumentStatusResponse;
import com.tmk.api.user.document.dto.DocumentUploadResponse;
import com.tmk.api.user.document.request.DocumentUploadRequest;
import com.tmk.api.user.document.usecase.DocumentUseCase;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/my/documents")
@RequiredArgsConstructor
public class DocumentController implements DocumentControllerDocs {

    private final DocumentUseCase documentUseCase;

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<List<DocumentStatusResponse>>> getDocuments(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        return ApiResponse.ok(documentUseCase.getDocuments(principal.getPrincipalId()));
    }

    @GetMapping("/{documentId}/status")
    @Override
    public ResponseEntity<ApiResponse<DocumentStatusResponse>> getDocumentStatus(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long documentId
    ) {
        return ApiResponse.ok(documentUseCase.getDocumentStatus(principal.getPrincipalId(), documentId));
    }

    @PostMapping("/upload")
    @Override
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @ModelAttribute DocumentUploadRequest request
    ) {
        return ApiResponse.ok(documentUseCase.uploadDocument(principal.getPrincipalId(), request));
    }

    @GetMapping(value = "/{documentId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public SseEmitter subscribeDocumentEvents(
            @PathVariable Long documentId,
            @RequestParam String accessToken
    ) {
        return documentUseCase.subscribeDocumentEvents(documentId, accessToken);
    }
}
