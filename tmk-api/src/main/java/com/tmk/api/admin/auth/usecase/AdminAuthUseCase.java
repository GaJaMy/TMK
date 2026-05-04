package com.tmk.api.admin.auth.usecase;

import com.tmk.api.admin.auth.dto.AdminLoginResponse;
import com.tmk.api.admin.auth.request.AdminLoginRequest;
import com.tmk.api.admin.auth.result.AdminLoginResult;
import com.tmk.api.admin.auth.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminAuthUseCase {

    private final AdminAuthService adminAuthService;

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminLoginResult result = adminAuthService.login(request.username(), request.password());
        return AdminLoginResponse.from(result);
    }
}
