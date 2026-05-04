package com.tmk.api.admin.question.controller;

import com.tmk.api.admin.question.dto.AdminPublicQuestionDetailResponse;
import com.tmk.api.admin.question.dto.AdminPublicQuestionSummaryResponse;
import com.tmk.api.admin.question.request.AdminPublicQuestionCreateRequest;
import com.tmk.api.admin.question.request.AdminPublicQuestionStatusChangeRequest;
import com.tmk.api.common.ApiResponse;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface AdminPublicQuestionControllerDocs {

    @Operation(summary = "공용 문제 목록 조회", description = "조건에 따라 공용 문제 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    ResponseEntity<ApiResponse<List<AdminPublicQuestionSummaryResponse>>> getPublicQuestions(
            @RequestParam(value = "topicId", required = false) Long topicId,
            @RequestParam(value = "difficulty", required = false) Difficulty difficulty,
            @RequestParam(value = "type", required = false) QuestionType type,
            @RequestParam(value = "active", required = false) Boolean active
    );

    @Operation(summary = "공용 문제 상세 조회", description = "공용 문제 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공용 문제 없음")
    })
    ResponseEntity<ApiResponse<AdminPublicQuestionDetailResponse>> getPublicQuestion(
            @PathVariable("questionId") Long questionId
    );

    @Operation(summary = "공용 문제 등록", description = "현재 로그인한 관리자가 공용 문제를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Topic 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "비활성 Topic 또는 선택지 규칙 오류")
    })
    ResponseEntity<ApiResponse<AdminPublicQuestionDetailResponse>> createPublicQuestion(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AdminPublicQuestionCreateRequest request
    );

    @Operation(summary = "공용 문제 상태 변경", description = "공용 문제의 활성/비활성 상태를 변경합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공용 문제 없음")
    })
    ResponseEntity<ApiResponse<AdminPublicQuestionDetailResponse>> changePublicQuestionStatus(
            @PathVariable("questionId") Long questionId,
            @Valid @RequestBody AdminPublicQuestionStatusChangeRequest request
    );

    @Operation(summary = "공용 문제 삭제", description = "공용 문제를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공용 문제 없음")
    })
    ResponseEntity<ApiResponse<Void>> deletePublicQuestion(
            @PathVariable("questionId") Long questionId
    );
}
