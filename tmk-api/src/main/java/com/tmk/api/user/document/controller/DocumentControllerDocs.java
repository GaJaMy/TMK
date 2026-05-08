package com.tmk.api.user.document.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.user.document.dto.DocumentStatusResponse;
import com.tmk.api.user.document.dto.DocumentUploadResponse;
import com.tmk.api.user.document.request.DocumentUploadRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface DocumentControllerDocs {

    @Operation(summary = "내 문서 목록 조회", description = "현재 로그인한 사용자의 문서 목록과 생성 상태를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<DocumentStatusResponse>>> getDocuments(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal
    );

    @Operation(summary = "내 문서 상태 조회", description = "특정 문서의 현재 생성 상태를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문서를 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<DocumentStatusResponse>> getDocumentStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long documentId
    );

    @Operation(summary = "문서 업로드 등록", description = "PDF 또는 MD 문서를 업로드하고 문제 생성용 문서를 PROCESSING 상태로 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 지원하지 않는 문서 형식"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = DocumentUploadRequest.class)
            )
    )
    ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @ModelAttribute DocumentUploadRequest request
    );

    @Operation(summary = "내 문서 상태 SSE 구독", description = "문서 상태 변경 이벤트를 SSE로 구독합니다. 브라우저 EventSource 제약 때문에 accessToken을 쿼리 파라미터로 전달합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "구독 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 액세스 토큰"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문서를 찾을 수 없음")
    })
    SseEmitter subscribeDocumentEvents(
            @PathVariable Long documentId,
            @RequestParam String accessToken
    );
}
