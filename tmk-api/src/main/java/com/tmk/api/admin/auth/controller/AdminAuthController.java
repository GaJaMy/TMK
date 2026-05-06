package com.tmk.api.admin.auth.controller;

import com.tmk.api.admin.auth.dto.AdminLoginResponse;
import com.tmk.api.admin.auth.request.AdminLoginRequest;
import com.tmk.api.admin.auth.request.AdminReissueRequest;
import com.tmk.api.admin.auth.usecase.AdminAuthUseCase;
import com.tmk.api.common.ApiVersion;
import com.tmk.api.common.ApiResponse;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.security.jwt.JwtAuthenticationFilter;
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
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController implements AdminAuthControllerDocs {

    private final AdminAuthUseCase adminAuthUseCase;

    @PostMapping(ApiVersion.V1 + "/login")
    @Override
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest request
    ) {
        return ApiResponse.ok(adminAuthUseCase.login(request));
    }

    @PostMapping(ApiVersion.V1 + "/reissue")
    @Override
    public ResponseEntity<ApiResponse<AdminLoginResponse>> reissue(
            @Valid @RequestBody AdminReissueRequest request
    ) {
        return ApiResponse.ok(adminAuthUseCase.reissue(request));
    }

    @PostMapping(ApiVersion.V1 + "/logout")
    @Override
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            HttpServletRequest request
    ) {
        String accessToken = (String) request.getAttribute(JwtAuthenticationFilter.ACCESS_TOKEN_ATTRIBUTE);
        adminAuthUseCase.logout(principal.getPrincipalId(), accessToken);
        return ApiResponse.noContent();
    }
}
