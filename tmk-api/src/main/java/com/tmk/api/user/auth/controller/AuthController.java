package com.tmk.api.user.auth.controller;

import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.security.jwt.JwtAuthenticationFilter;
import com.tmk.api.common.ApiResponse;
import com.tmk.api.common.ApiVersion;
import com.tmk.api.user.auth.dto.LoginResponse;
import com.tmk.api.user.auth.dto.RegisterResponse;
import com.tmk.api.user.auth.request.LoginRequest;
import com.tmk.api.user.auth.request.RegisterRequest;
import com.tmk.api.user.auth.request.ReissueRequest;
import com.tmk.api.user.auth.request.ResetPasswordRequest;
import com.tmk.api.user.auth.usecase.AuthUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthUseCase authUseCase;

    @PostMapping(ApiVersion.V1 + "/login")
    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponse.ok(authUseCase.login(request));
    }

    @PostMapping(ApiVersion.V1 + "/register")
    @Override
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ApiResponse.ok(authUseCase.register(request));
    }

    @PostMapping(ApiVersion.V1 + "/reissue")
    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        return ApiResponse.ok(authUseCase.reissue(request));
    }

    @PostMapping(ApiVersion.V1 + "/logout")
    @Override
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            HttpServletRequest request
    ) {
        String accessToken = (String) request.getAttribute(JwtAuthenticationFilter.ACCESS_TOKEN_ATTRIBUTE);
        authUseCase.logout(principal.getPrincipalId(), accessToken);
        return ApiResponse.noContent();
    }

    @PostMapping(ApiVersion.V1 + "/reset-password")
    @Override
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authUseCase.resetPassword(request);
        return ApiResponse.ok();
    }
}
