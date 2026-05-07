package com.tmk.api.user.exam.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.user.exam.dto.ExamCreateResponse;
import com.tmk.api.user.exam.dto.ExamDetailResponse;
import com.tmk.api.user.exam.dto.ExamHistorySummaryResponse;
import com.tmk.api.user.exam.dto.ExamResultDetailResponse;
import com.tmk.api.user.exam.dto.ExamSummaryResponse;
import com.tmk.api.user.exam.dto.ExamStartResponse;
import com.tmk.api.user.exam.request.ExamAnswerSaveRequest;
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

    @Operation(summary = "시험 히스토리 조회", description = "현재 로그인한 사용자의 제출 완료 시험 이력을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<ExamHistorySummaryResponse>>> getExamHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal
    );

    @Operation(summary = "시험 문제 조회", description = "진행 중인 시험의 문제 목록과 현재 저장된 답안을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "진행 중인 시험이 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "시험 시간이 만료됨")
    })
    ResponseEntity<ApiResponse<ExamDetailResponse>> getExam(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long examId
    );

    @Operation(summary = "시험 결과 조회", description = "제출이 완료된 시험의 결과와 문항별 정답/해설을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "시험 결과를 아직 조회할 수 없음")
    })
    ResponseEntity<ApiResponse<ExamResultDetailResponse>> getExamResult(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long examId
    );

    @Operation(summary = "답안 저장", description = "진행 중인 시험 문항의 답안을 저장합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 또는 시험 문항을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "진행 중인 시험이 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "시험 시간이 만료됨")
    })
    ResponseEntity<ApiResponse<Void>> saveAnswers(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long examId,
            @Valid @RequestBody List<@Valid ExamAnswerSaveRequest> requests
    );

    @Operation(summary = "시험 제출", description = "진행 중인 시험을 채점하고 최종 제출합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "제출 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "진행 중인 시험이 아니거나 이미 제출됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "시험 시간이 만료되어 자동 제출됨")
    })
    ResponseEntity<ApiResponse<Void>> submitExam(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long examId
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
