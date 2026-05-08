package com.tmk.api.admin.auth.result;

public record AdminLoginResult(
        Long adminId,
        String username,
        String accessToken,
        String refreshToken,
        long expiresIn,
        String role
) {
}
