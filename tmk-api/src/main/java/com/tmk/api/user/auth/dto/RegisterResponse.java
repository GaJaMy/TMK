package com.tmk.api.user.auth.dto;

import com.tmk.api.user.auth.result.RegisterResult;

public record RegisterResponse(
        Long userId,
        String username
) {

    public static RegisterResponse from(RegisterResult result) {
        return new RegisterResponse(
                result.userId(),
                result.username()
        );
    }
}
