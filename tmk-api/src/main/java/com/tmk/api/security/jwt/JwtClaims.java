package com.tmk.api.security.jwt;

public record JwtClaims(Long userId, String email, String role) {}
