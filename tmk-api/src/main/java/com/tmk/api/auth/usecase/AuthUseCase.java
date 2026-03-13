package com.tmk.api.auth.usecase;

import com.tmk.api.auth.dto.LoginResult;
import com.tmk.api.auth.dto.ReissueResult;
import com.tmk.api.auth.dto.SocialLoginResult;
import com.tmk.api.security.jwt.JwtProperties;
import com.tmk.api.security.jwt.JwtProvider;
import com.tmk.core.auth.service.*;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.UserPort;
import com.tmk.core.user.entity.User;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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
        User user = loginService.login(email, rawPassword);

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // Store refresh token in Redis
        redisTemplate.opsForValue().set(
                "refresh:" + user.getId(),
                refreshToken,
                jwtProperties.getRefreshTokenExpiry(),
                TimeUnit.MILLISECONDS
        );

        return new LoginResult(accessToken, refreshToken, jwtProperties.getAccessTokenExpiry());
    }

    public void logout(String accessToken) {
        // Register access token in Redis blacklist
        try {
            Claims claims = jwtProvider.parseClaims(accessToken);
            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remaining > 0) {
                redisTemplate.opsForValue().set(
                        "blacklist:" + accessToken,
                        "logout",
                        remaining,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception ignored) {
        }
        logoutService.logout(accessToken);
    }

    public ReissueResult reissue(String refreshToken) {
        // Extract userId from refresh token
        Claims claims = jwtProvider.parseClaims(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());

        // Validate stored refresh token in Redis
        String stored = redisTemplate.opsForValue().get("refresh:" + userId);
        if (!refreshToken.equals(stored)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // Lookup user to get accurate role information
        User user = userPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        String newAccessToken = jwtProvider.generateAccessToken(userId, user.getEmail(), user.getRole().name());

        return new ReissueResult(newAccessToken, jwtProperties.getAccessTokenExpiry());
    }

    public SocialLoginResult socialLogin(String provider, String code) {
        throw new UnsupportedOperationException("Social login not implemented");
    }
}
