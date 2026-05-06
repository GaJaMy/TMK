package com.tmk.api.security.jwt;

import com.tmk.api.security.AuthenticatedPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey(String principalType) {
        return Keys.hmacShaKeyFor(getTokenProperties(principalType).getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long principalId, String username, String role, String principalType) {
        return Jwts.builder()
                .subject(username)
                .claim("principalId", principalId)
                .claim("role", role)
                .claim("principalType", principalType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getAccessTokenExpiry(principalType)))
                .signWith(getSigningKey(principalType))
                .compact();
    }

    public String generateRefreshToken(Long principalId, String principalType) {
        return Jwts.builder()
                .subject(String.valueOf(principalId))
                .claim("principalType", principalType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getRefreshTokenExpiry(principalType)))
                .signWith(getSigningKey(principalType))
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return parseWithFallback(token, this::parseClaimsWithSigningKey);
    }

    public long getAccessTokenExpiry(String principalType) {
        return getTokenProperties(principalType).getAccessTokenExpiry();
    }

    public long getRefreshTokenExpiry(String principalType) {
        return getTokenProperties(principalType).getRefreshTokenExpiry();
    }

    public Duration getRemainingValidity(String token) {
        Claims claims = parseClaims(token);
        long remainingMillis = claims.getExpiration().getTime() - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(remainingMillis, 0));
    }

    private Claims parseClaimsWithSigningKey(String token, String principalType) {
        return Jwts.parser()
                .verifyWith(getSigningKey(principalType))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T parseWithFallback(String token, TokenParser<T> parser) {
        JwtException lastJwtException = null;
        IllegalArgumentException lastIllegalArgumentException = null;

        try {
            return parser.parse(token, AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException e) {
            lastJwtException = e;
        } catch (IllegalArgumentException e) {
            lastIllegalArgumentException = e;
        }

        try {
            return parser.parse(token, AuthenticatedPrincipal.USER_PRINCIPAL_TYPE);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException e) {
            if (lastJwtException != null) {
                throw lastJwtException;
            }
            throw e;
        } catch (IllegalArgumentException e) {
            if (lastIllegalArgumentException != null) {
                throw lastIllegalArgumentException;
            }
            throw e;
        }
    }

    private JwtProperties.TokenProperties getTokenProperties(String principalType) {
        if (AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE.equals(principalType)) {
            return jwtProperties.getAdmin();
        }
        if (AuthenticatedPrincipal.USER_PRINCIPAL_TYPE.equals(principalType)) {
            return jwtProperties.getUser();
        }
        throw new IllegalArgumentException("지원하지 않는 principalType 입니다: " + principalType);
    }

    @FunctionalInterface
    private interface TokenParser<T> {
        T parse(String token, String principalType);
    }
}
