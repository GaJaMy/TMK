package com.tmk.api.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long principalId, String username, String role, String principalType) {
        return Jwts.builder()
                .subject(username)
                .claim("principalId", principalId)
                .claim("role", role)
                .claim("principalType", principalType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiry()))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long principalId, String principalType) {
        return Jwts.builder()
                .subject(String.valueOf(principalId))
                .claim("principalType", principalType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiry()))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTokenExpiry() {
        return jwtProperties.getAccessTokenExpiry();
    }

    public long getRefreshTokenExpiry() {
        return jwtProperties.getRefreshTokenExpiry();
    }
}
