package com.tmk.core.admin.vo;

public record AdminLoginResult(
        Long adminId,
        String username,
        String role
) {
}
