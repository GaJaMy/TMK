package com.tmk.api.user.auth.result;

public record LoginResult(
        Long userId,
        String username,
        String accessToken,
        String refreshToken,
        long expiresIn,
        String role
) {
}
