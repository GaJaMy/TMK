package com.tmk.api.document.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.document.dto.DocumentStatusResult;
import com.tmk.api.document.dto.RegisterDocumentResult;
import com.tmk.api.document.request.RegisterDocumentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Document (Internal)", description = "문서 관리 내부 API")
public interface DocumentControllerDocs {

    @Operation(summary = "문서 등록", description = "새 문서를 등록하고 파이프라인 처리를 시작합니다. (내부 전용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "문서 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    ResponseEntity<ApiResponse<RegisterDocumentResult>> register(
            @RequestBody(description = "등록할 문서 정보", required = true)
            RegisterDocumentRequest request
    );

    @Operation(summary = "문서 파일 업로드", description = "PDF 파일을 업로드하고 저장된 경로로 문서 등록을 진행합니다. (내부 전용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "문서 업로드 및 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파일 또는 요청 데이터")
    })
    ResponseEntity<ApiResponse<RegisterDocumentResult>> upload(
            @Parameter(description = "문서 제목", required = true) String title,
            @Parameter(description = "문서 주제", required = true) String topic,
            @Parameter(description = "업로드할 파일", required = true) MultipartFile file
    );

    @Operation(summary = "문서 처리 상태 조회", description = "문서의 현재 처리 상태를 조회합니다. (내부 전용)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문서를 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<DocumentStatusResult>> getStatus(
            @Parameter(description = "조회할 문서 ID", required = true)
            Long documentId
    );
}
