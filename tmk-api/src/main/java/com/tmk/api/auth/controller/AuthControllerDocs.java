package com.tmk.api.auth.controller;

import com.tmk.api.auth.dto.LoginResult;
import com.tmk.api.auth.dto.ReissueResult;
import com.tmk.api.auth.dto.SocialLoginResult;
import com.tmk.api.auth.request.LoginRequest;
import com.tmk.api.auth.request.ReissueRequest;
import com.tmk.api.auth.request.RegisterRequest;
import com.tmk.api.auth.request.SendVerificationRequest;
import com.tmk.api.auth.request.SocialLoginRequest;
import com.tmk.api.auth.request.VerifyRequest;
import com.tmk.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "인증 API")
public interface AuthControllerDocs {

    @Operation(summary = "이메일 인증 코드 발송", description = "주어진 이메일 주소로 인증 코드를 발송합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 이메일 형식")
    })
    ResponseEntity<ApiResponse<Void>> sendVerification(
            @RequestBody(description = "인증 코드를 발송할 이메일 주소", required = true)
            SendVerificationRequest request
    );

    @Operation(summary = "이메일 인증 코드 확인", description = "발송된 인증 코드의 유효성을 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 인증 코드 또는 만료된 코드")
    })
    ResponseEntity<ApiResponse<Void>> verify(
            @RequestBody(description = "이메일 및 인증 코드", required = true)
            VerifyRequest request
    );

    @Operation(summary = "회원가입", description = "이메일 인증 완료 후 회원가입을 진행합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검사 실패 또는 이미 존재하는 이메일")
    })
    ResponseEntity<ApiResponse<Void>> register(
            @RequestBody(description = "회원가입 정보", required = true)
            RegisterRequest request
    );

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공, 액세스 토큰 및 리프레시 토큰 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    ResponseEntity<ApiResponse<LoginResult>> login(
            @RequestBody(description = "로그인 자격 증명", required = true)
            LoginRequest request
    );

    @Operation(summary = "로그아웃", description = "현재 세션의 액세스 토큰을 무효화합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "Bearer 액세스 토큰", required = true)
            String authorizationHeader
    );

    @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰을 사용하여 새 액세스 토큰을 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰")
    })
    ResponseEntity<ApiResponse<ReissueResult>> reissue(
            @RequestBody(description = "리프레시 토큰", required = true)
            ReissueRequest request
    );

    @Operation(summary = "소셜 로그인", description = "소셜 OAuth2 code를 사용하여 로그인하거나 회원가입합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "소셜 로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 소셜 로그인 코드")
    })
    ResponseEntity<ApiResponse<SocialLoginResult>> socialLogin(
            @Parameter(description = "소셜 로그인 제공자 (예: google, kakao)", required = true)
            String provider,
            @RequestBody(description = "소셜 OAuth2 인가 코드", required = true)
            SocialLoginRequest request
    );
}
