package com.tmk.api.user.auth.dto;

import com.tmk.api.user.auth.result.LoginResult;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String role
) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn(),
                result.role()
        );
    }
}
