package com.tmk.api.admin.auth.usecase;

import com.tmk.api.admin.auth.dto.AdminLoginResponse;
import com.tmk.api.security.jwt.JwtProvider;
import com.tmk.core.admin.service.AdminLoginService;
import com.tmk.core.admin.vo.AdminLoginResult;
import com.tmk.core.port.out.cache.RefreshTokenPort;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAuthUseCase {

    private static final String ADMIN_PRINCIPAL_TYPE = "ADMIN";

    private final AdminLoginService adminLoginService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenPort refreshTokenPort;

    public AdminLoginResponse login(String username, String password) {
        AdminLoginResult result = adminLoginService.login(username, password);

        String accessToken = jwtProvider.generateAccessToken(
                result.adminId(),
                result.username(),
                result.role(),
                ADMIN_PRINCIPAL_TYPE
        );
        String refreshToken = jwtProvider.generateRefreshToken(result.adminId(), ADMIN_PRINCIPAL_TYPE);

        refreshTokenPort.save(
                ADMIN_PRINCIPAL_TYPE,
                result.adminId(),
                refreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiry())
        );

        return new AdminLoginResponse(
                result.adminId(),
                result.username(),
                accessToken,
                refreshToken,
                jwtProvider.getAccessTokenExpiry()
        );
    }
}
