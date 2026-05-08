package com.tmk.api.admin.account.controller;

import com.tmk.api.admin.account.dto.AdminAccountSummaryResponse;
import com.tmk.api.admin.account.request.AdminAccountCreateRequest;
import com.tmk.api.admin.account.request.AdminAccountStatusChangeRequest;
import com.tmk.api.common.ApiResponse;
import com.tmk.api.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface AdminAccountControllerDocs {

    @Operation(summary = "관리자 목록 조회", description = "등록된 관리자 계정 목록을 생성일시 역순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    ResponseEntity<ApiResponse<List<AdminAccountSummaryResponse>>> getAdminAccounts();

    @Operation(summary = "관리자 계정 생성", description = "현재 로그인한 관리자가 새 관리자 계정을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 아이디")
    })
    ResponseEntity<ApiResponse<AdminAccountSummaryResponse>> createAdminAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AdminAccountCreateRequest request
    );

    @Operation(summary = "관리자 계정 상태 변경", description = "관리자 계정의 활성/비활성 상태를 변경합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "관리자 계정 없음")
    })
    ResponseEntity<ApiResponse<AdminAccountSummaryResponse>> changeAdminAccountStatus(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody AdminAccountStatusChangeRequest request
    );

    @Operation(summary = "관리자 계정 삭제", description = "관리자 계정을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "관리자 계정 없음")
    })
    ResponseEntity<ApiResponse<Void>> deleteAdminAccount(
            @PathVariable("userId") Long userId
    );
}
