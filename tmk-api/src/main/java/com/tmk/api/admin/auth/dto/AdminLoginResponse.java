package com.tmk.api.admin.auth.dto;

import com.tmk.api.admin.auth.result.AdminLoginResult;

public record AdminLoginResponse(
        Long adminId,
        String username,
        String accessToken,
        String refreshToken,
        long expiresIn,
        String role
) {

    public static AdminLoginResponse from(AdminLoginResult result) {
        return new AdminLoginResponse(
                result.adminId(),
                result.username(),
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn(),
                result.role()
        );
    }
}
