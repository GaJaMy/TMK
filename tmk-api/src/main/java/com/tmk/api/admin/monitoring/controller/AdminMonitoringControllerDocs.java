package com.tmk.api.admin.monitoring.controller;

import com.tmk.api.admin.monitoring.dto.AdminMonitoringStatResponse;
import com.tmk.api.admin.monitoring.request.AdminMonitoringStatRequest;
import com.tmk.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface AdminMonitoringControllerDocs {

    @Operation(summary = "사용자 웹 접근 시도 통계 조회", description = "기간 단위와 조회 기간에 따라 사용자 웹 접근 시도 통계를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류 또는 기간 조건 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    ResponseEntity<ApiResponse<AdminMonitoringStatResponse>> getUserPageAccessAttempts(
            @Valid @ModelAttribute AdminMonitoringStatRequest request
    );

    @Operation(summary = "시험 진행 통계 조회", description = "기간 단위와 조회 기간에 따라 시험 진행 통계를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류 또는 기간 조건 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    ResponseEntity<ApiResponse<AdminMonitoringStatResponse>> getExamRuns(
            @Valid @ModelAttribute AdminMonitoringStatRequest request
    );

    @Operation(summary = "사용자 문서 등록 통계 조회", description = "기간 단위와 조회 기간에 따라 사용자 문서 등록 통계를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류 또는 기간 조건 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    ResponseEntity<ApiResponse<AdminMonitoringStatResponse>> getDocumentRegistrations(
            @Valid @ModelAttribute AdminMonitoringStatRequest request
    );

    @Operation(summary = "사용자 문제 생성 통계 조회", description = "기간 단위와 조회 기간에 따라 사용자 문제 생성 통계를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류 또는 기간 조건 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    ResponseEntity<ApiResponse<AdminMonitoringStatResponse>> getQuestionGenerations(
            @Valid @ModelAttribute AdminMonitoringStatRequest request
    );
}
