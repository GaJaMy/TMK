package com.tmk.api.document.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.document.dto.DocumentStatusResult;
import com.tmk.api.document.dto.RegisterDocumentResult;
import com.tmk.api.document.request.RegisterDocumentRequest;
import com.tmk.api.document.usecase.DocumentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/documents")
@RequiredArgsConstructor
public class DocumentController implements DocumentControllerDocs {

    private final DocumentUseCase documentUseCase;

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<RegisterDocumentResult>> register(
            @RequestBody RegisterDocumentRequest request
    ) {
        RegisterDocumentResult result = documentUseCase.register(request.getTitle(), request.getSource());
        return ApiResponse.ok(result);
    }

    @GetMapping("/{documentId}/status")
    @Override
    public ResponseEntity<ApiResponse<DocumentStatusResult>> getStatus(
            @PathVariable Long documentId
    ) {
        DocumentStatusResult result = documentUseCase.getStatus(documentId);
        return ApiResponse.ok(result);
    }
}
