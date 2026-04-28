package com.tmk.api.admin.auth.controller;

import com.tmk.api.admin.auth.dto.AdminLoginResponse;
import com.tmk.api.admin.auth.request.AdminLoginRequest;
import com.tmk.api.admin.auth.usecase.AdminAuthUseCase;
import com.tmk.api.common.ApiVersion;
import com.tmk.api.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        AdminLoginResponse response = adminAuthUseCase.login(request.username(), request.password());
        return ApiResponse.ok(response);
    }
}
