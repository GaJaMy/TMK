package com.tmk.api.admin.auth.dto;

public record AdminLoginResponse(
        Long adminId,
        String username,
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
