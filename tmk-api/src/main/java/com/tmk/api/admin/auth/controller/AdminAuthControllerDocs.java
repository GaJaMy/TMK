package com.tmk.api.admin.auth.controller;

import com.tmk.api.admin.auth.dto.AdminLoginResponse;
import com.tmk.api.admin.auth.request.AdminLoginRequest;
import com.tmk.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface AdminAuthControllerDocs {

    @Operation(summary = "관리자 로그인", description = "관리자 계정으로 로그인하고 access token, refresh token을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "비활성 관리자 계정")
    })
    ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest request
    );
}
