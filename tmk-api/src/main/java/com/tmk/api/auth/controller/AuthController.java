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
import com.tmk.api.auth.usecase.AuthUseCase;
import com.tmk.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthUseCase authUseCase;

    @PostMapping("/verification/send")
    @Override
    public ResponseEntity<ApiResponse<Void>> sendVerification(
            @RequestBody SendVerificationRequest request
    ) {
        authUseCase.sendVerification(request.getEmail());
        return ApiResponse.ok();
    }

    @PostMapping("/verification/verify")
    @Override
    public ResponseEntity<ApiResponse<Void>> verify(
            @RequestBody VerifyRequest request
    ) {
        authUseCase.verify(request.getEmail(), request.getCode());
        return ApiResponse.ok();
    }

    @PostMapping("/register")
    @Override
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody RegisterRequest request
    ) {
        authUseCase.register(request.getEmail(), request.getPassword());
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<ApiResponse<LoginResult>> login(
            @RequestBody LoginRequest request
    ) {
        LoginResult result = authUseCase.login(request.getEmail(), request.getPassword());
        return ApiResponse.ok(result);
    }

    @PostMapping("/logout")
    @Override
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : authorizationHeader;
        authUseCase.logout(token);
        return ApiResponse.ok();
    }

    @PostMapping("/reissue")
    @Override
    public ResponseEntity<ApiResponse<ReissueResult>> reissue(
            @RequestBody ReissueRequest request
    ) {
        ReissueResult result = authUseCase.reissue(request.getRefreshToken());
        return ApiResponse.ok(result);
    }

    @PostMapping("/social/{provider}")
    @Override
    public ResponseEntity<ApiResponse<SocialLoginResult>> socialLogin(
            @PathVariable String provider,
            @RequestBody SocialLoginRequest request
    ) {
        SocialLoginResult result = authUseCase.socialLogin(provider, request.getCode());
        return ApiResponse.ok(result);
    }
}
