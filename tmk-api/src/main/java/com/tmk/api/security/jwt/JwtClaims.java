package com.tmk.api.security.jwt;

public record JwtClaims(
        Long principalId,
        String username,
        String role,
        String principalType
) {
}
