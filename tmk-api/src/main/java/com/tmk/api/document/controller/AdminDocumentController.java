package com.tmk.api.document.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.document.FileDocumentStorage;
import com.tmk.api.document.dto.DocumentStatusResult;
import com.tmk.api.document.dto.RegisterDocumentResult;
import com.tmk.api.document.request.RegisterDocumentRequest;
import com.tmk.api.document.usecase.DocumentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/admin/v1/documents")
@RequiredArgsConstructor
public class AdminDocumentController {

    private final DocumentUseCase documentUseCase;
    private final FileDocumentStorage fileDocumentStorage;

    @PostMapping
    public ResponseEntity<ApiResponse<RegisterDocumentResult>> register(@RequestBody RegisterDocumentRequest request) {
        return ApiResponse.ok(documentUseCase.registerPublic(request.getTitle(), request.getSource(), request.getTopic()));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<RegisterDocumentResult>> upload(
            @RequestParam("title") String title,
            @RequestParam("topic") String topic,
            @RequestParam("file") MultipartFile file
    ) {
        String storedPath = fileDocumentStorage.store(file);
        return ApiResponse.ok(documentUseCase.registerPublic(title, storedPath, topic));
    }

    @GetMapping("/{documentId}/status")
    public ResponseEntity<ApiResponse<DocumentStatusResult>> getStatus(@PathVariable Long documentId) {
        return ApiResponse.ok(documentUseCase.getStatus(documentId));
    }

    @GetMapping(value = "/{documentId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable Long documentId,
            @RequestParam("accessToken") String accessToken
    ) {
        return documentUseCase.subscribeAdmin(documentId, accessToken);
    }
}
