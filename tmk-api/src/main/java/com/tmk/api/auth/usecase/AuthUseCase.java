package com.tmk.api.auth.usecase;

import com.tmk.api.auth.dto.LoginResult;
import com.tmk.api.auth.dto.ReissueResult;
import com.tmk.api.auth.dto.SocialLoginResult;
import com.tmk.api.security.jwt.JwtProperties;
import com.tmk.api.security.jwt.JwtProvider;
import com.tmk.core.auth.service.*;
import com.tmk.core.port.out.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUseCase {

    private final SendEmailVerificationService sendEmailVerificationService;
    private final VerifyEmailService verifyEmailService;
    private final RegisterUserService registerUserService;
    private final LoginService loginService;
    private final LogoutService logoutService;
    private final ReissueTokenService reissueTokenService;
    private final SocialLoginService socialLoginService;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserPort userPort;

    public void sendVerification(String email) {
        sendEmailVerificationService.sendVerification(email);
    }

    public void verify(String email, String code) {
        verifyEmailService.verify(email, code);
    }

    public void register(String email, String rawPassword) {
        registerUserService.register(email, rawPassword);
    }

    public LoginResult login(String email, String rawPassword) {
        com.tmk.core.user.entity.User user = loginService.login(email, rawPassword);
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());
        long refreshTtlSeconds = jwtProperties.getRefreshTokenExpiry() / 1000;
        redisTemplate.opsForValue().set(
                "refresh_token:" + user.getId(),
                refreshToken,
                refreshTtlSeconds,
                java.util.concurrent.TimeUnit.SECONDS
        );
        return new LoginResult(accessToken, refreshToken, jwtProperties.getAccessTokenExpiry());
    }

    public void logout(String accessToken) {
        io.jsonwebtoken.Claims claims = jwtProvider.parseClaims(accessToken);
        Long userId = claims.get("userId", Long.class);
        java.util.Date expiration = claims.getExpiration();
        long remainingMs = expiration.getTime() - System.currentTimeMillis();
        if (remainingMs > 0) {
            long remainingSeconds = remainingMs / 1000;
            redisTemplate.opsForValue().set(
                    "token_blacklist:" + accessToken,
                    "true",
                    remainingSeconds,
                    java.util.concurrent.TimeUnit.SECONDS
            );
        }
        redisTemplate.delete("refresh_token:" + userId);
    }

    public ReissueResult reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new com.tmk.core.exception.BusinessException(
                    com.tmk.core.exception.ErrorCode.REFRESH_TOKEN_INVALID);
        }
        io.jsonwebtoken.Claims claims = jwtProvider.parseClaims(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());
        String stored = redisTemplate.opsForValue().get("refresh_token:" + userId);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new com.tmk.core.exception.BusinessException(
                    com.tmk.core.exception.ErrorCode.REFRESH_TOKEN_INVALID);
        }
        com.tmk.core.user.entity.User user = userPort.findById(userId)
                .orElseThrow(() -> new com.tmk.core.exception.BusinessException(
                        com.tmk.core.exception.ErrorCode.REFRESH_TOKEN_INVALID));
        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        return new ReissueResult(newAccessToken, jwtProperties.getAccessTokenExpiry());
    }

    public SocialLoginResult socialLogin(String provider, String code) {
        socialLoginService.socialLogin(provider, code);
        // TODO: build SocialLoginResult
        return null;
    }
}
