package com.tmk.api.auth.usecase;

import com.tmk.api.auth.dto.LoginResult;
import com.tmk.api.auth.dto.ReissueResult;
import com.tmk.api.auth.dto.SocialLoginResult;
import com.tmk.api.security.CustomUserDetails;
import com.tmk.api.security.jwt.JwtProperties;
import com.tmk.api.security.jwt.JwtProvider;
import com.tmk.core.auth.service.*;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.UserPort;
import com.tmk.core.user.entity.User;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUseCase {

    private final SendEmailVerificationService sendEmailVerificationService;
    private final VerifyEmailService verifyEmailService;
    private final RegisterUserService registerUserService;
    private final SocialLoginService socialLoginService;
    private final AuthenticationManager authenticationManager;
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
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, rawPassword)
            );
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtProvider.generateAccessToken(userDetails.getUserId(), userDetails.getUsername(), userDetails.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(userDetails.getUserId());
        long refreshTtlSeconds = jwtProperties.getRefreshTokenExpiry() / 1000;
        redisTemplate.opsForValue().set(
                "refresh_token:" + userDetails.getUserId(),
                refreshToken,
                refreshTtlSeconds,
                java.util.concurrent.TimeUnit.SECONDS
        );
        return new LoginResult(accessToken, refreshToken, jwtProperties.getAccessTokenExpiry());
    }

    public void logout(String accessToken) {
        Claims claims = jwtProvider.parseClaims(accessToken);
        Long userId = claims.get("userId", Long.class);
        Date expiration = claims.getExpiration();
        long remainingMs = expiration.getTime() - System.currentTimeMillis();
        if (remainingMs > 0) {
            redisTemplate.opsForValue().set(
                    "token_blacklist:" + accessToken,
                    "true",
                    remainingMs / 1000,
                    TimeUnit.SECONDS
            );
        }
        redisTemplate.delete("refresh_token:" + userId);
    }

    public ReissueResult reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        Claims claims = jwtProvider.parseClaims(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());
        String stored = redisTemplate.opsForValue().get("refresh_token:" + userId);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        User user = userPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        return new ReissueResult(newAccessToken, jwtProperties.getAccessTokenExpiry());
    }

    public SocialLoginResult socialLogin(String provider, String code) {
        socialLoginService.socialLogin(provider, code);
        // TODO: build SocialLoginResult
        return null;
    }
}
