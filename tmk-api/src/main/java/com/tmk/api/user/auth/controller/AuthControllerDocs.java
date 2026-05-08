package com.tmk.api.user.auth.controller;

import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.common.ApiResponse;
import com.tmk.api.user.auth.dto.LoginResponse;
import com.tmk.api.user.auth.dto.RegisterResponse;
import com.tmk.api.user.auth.request.LoginRequest;
import com.tmk.api.user.auth.request.RegisterRequest;
import com.tmk.api.user.auth.request.ReissueRequest;
import com.tmk.api.user.auth.request.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthControllerDocs {

    @Operation(summary = "사용자 로그인", description = "일반 사용자 계정으로 로그인하고 access token, refresh token을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "비활성 사용자 계정")
    })
    ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "회원가입", description = "일반 사용자 계정을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 아이디")
    })
    ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request);

    @Operation(summary = "사용자 토큰 재발급", description = "리프레시 토큰으로 access token, refresh token을 재발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    })
    ResponseEntity<ApiResponse<LoginResponse>> reissue(@Valid @RequestBody ReissueRequest request);

    @Operation(summary = "사용자 로그아웃", description = "현재 access token을 블랙리스트 처리하고 refresh token을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            HttpServletRequest request
    );

    @Operation(summary = "비밀번호 재설정", description = "아이디를 기준으로 사용자의 비밀번호를 재설정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재설정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "비밀번호 재설정 대상 없음")
    })
    ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request);
}
