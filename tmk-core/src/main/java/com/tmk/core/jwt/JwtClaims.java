package com.tmk.core.jwt;

public record JwtClaims(Long userId, String email, String role) {}
