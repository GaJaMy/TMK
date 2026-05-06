package com.tmk.api.user.exam.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.user.exam.dto.ExamCreateResponse;
import com.tmk.api.user.exam.dto.ExamSummaryResponse;
import com.tmk.api.user.exam.dto.ExamStartResponse;
import com.tmk.api.user.exam.request.ExamCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface ExamControllerDocs {

    @Operation(summary = "시험 목록 조회", description = "현재 로그인한 사용자의 CREATED, IN_PROGRESS 시험 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<ExamSummaryResponse>>> getExams(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal
    );

    @Operation(summary = "시험 생성", description = "공용 Topic 또는 개인 문서 기반으로 시험을 생성합니다. 생성 단계에서는 아직 시험이 시작되지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "시험 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Topic 또는 문서를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "비활성 Topic 또는 문서 상태 미완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "문제 수 부족")
    })
    ResponseEntity<ApiResponse<ExamCreateResponse>> createExam(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ExamCreateRequest request
    );

    @Operation(summary = "시험 시작", description = "생성된 시험을 실제로 시작합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "시험 시작 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 진행 중인 시험 존재 또는 이미 시작됨")
    })
    ResponseEntity<ApiResponse<ExamStartResponse>> startExam(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long examId
    );
}
