package com.tmk.api.user.auth.service;

import com.tmk.api.monitoring.event.UserWebAccessAttemptedEvent;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.security.UserAuthenticationProvider;
import com.tmk.api.security.jwt.JwtProvider;
import com.tmk.api.user.auth.result.LoginResult;
import com.tmk.api.user.auth.result.RegisterResult;
import io.jsonwebtoken.Claims;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.cache.RefreshTokenPort;
import com.tmk.core.port.out.cache.TokenBlacklistPort;
import com.tmk.core.port.out.persistence.UserAccountPort;
import com.tmk.core.port.out.security.PasswordEncoderPort;
import com.tmk.core.user.entity.UserAccount;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserAccountPort userAccountPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final UserAuthenticationProvider userAuthenticationProvider;
    private final JwtProvider jwtProvider;
    private final RefreshTokenPort refreshTokenPort;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public RegisterResult register(String username, String password, String countryCode) {
        if (userAccountPort.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        OffsetDateTime now = OffsetDateTime.now();
        String encodedPassword = passwordEncoderPort.encode(password);
        String normalizedCountryCode = countryCode.toUpperCase(Locale.ROOT);

        UserAccount userAccount = UserAccount.create(username, encodedPassword, normalizedCountryCode, now);
        UserAccount savedUserAccount = userAccountPort.save(userAccount);
        return RegisterResult.from(savedUserAccount);
    }

    @Transactional
    public LoginResult login(String username, String password) {
        Authentication authentication;
        try {
            authentication = userAuthenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (DisabledException e) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_INACTIVE);
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }

        AuthenticatedPrincipal userDetails = (AuthenticatedPrincipal) authentication.getPrincipal();

        String accessToken = jwtProvider.generateAccessToken(
                userDetails.getPrincipalId(),
                userDetails.getUsername(),
                userDetails.getRole(),
                AuthenticatedPrincipal.USER_PRINCIPAL_TYPE
        );
        String refreshToken = jwtProvider.generateRefreshToken(
                userDetails.getPrincipalId(),
                AuthenticatedPrincipal.USER_PRINCIPAL_TYPE
        );

        refreshTokenPort.save(
                AuthenticatedPrincipal.USER_PRINCIPAL_TYPE,
                userDetails.getPrincipalId(),
                refreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiry(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE))
        );
        applicationEventPublisher.publishEvent(new UserWebAccessAttemptedEvent(userDetails.getPrincipalId()));

        return new LoginResult(
                userDetails.getPrincipalId(),
                userDetails.getUsername(),
                accessToken,
                refreshToken,
                jwtProvider.getAccessTokenExpiry(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE),
                userDetails.getRole()
        );
    }

    @Transactional
    public LoginResult reissue(String refreshToken) {
        Claims claims;
        try {
            claims = jwtProvider.parseClaims(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String principalType = claims.get("principalType", String.class);
        if (!AuthenticatedPrincipal.USER_PRINCIPAL_TYPE.equals(principalType)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long principalId = Long.valueOf(claims.getSubject());
        String savedRefreshToken = refreshTokenPort.find(principalType, principalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!savedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        UserAccount userAccount = userAccountPort.findById(principalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!userAccount.isActive()) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_INACTIVE);
        }

        String newAccessToken = jwtProvider.generateAccessToken(
                userAccount.getId(),
                userAccount.getUsername(),
                AuthenticatedPrincipal.USER_ROLE,
                AuthenticatedPrincipal.USER_PRINCIPAL_TYPE
        );
        String newRefreshToken = jwtProvider.generateRefreshToken(
                userAccount.getId(),
                AuthenticatedPrincipal.USER_PRINCIPAL_TYPE
        );

        refreshTokenPort.save(
                AuthenticatedPrincipal.USER_PRINCIPAL_TYPE,
                userAccount.getId(),
                newRefreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiry(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE))
        );

        return new LoginResult(
                userAccount.getId(),
                userAccount.getUsername(),
                newAccessToken,
                newRefreshToken,
                jwtProvider.getAccessTokenExpiry(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE),
                AuthenticatedPrincipal.USER_ROLE
        );
    }

    @Transactional
    public void logout(Long principalId, String accessToken) {
        refreshTokenPort.delete(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE, principalId);
        Duration remainingValidity = jwtProvider.getRemainingValidity(accessToken);
        tokenBlacklistPort.blacklist(accessToken, remainingValidity);
    }

    @Transactional
    public void resetPassword(String username, String newPassword) {
        UserAccount userAccount = userAccountPort.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESET_PASSWORD_TARGET_NOT_FOUND));

        String encodedPassword = passwordEncoderPort.encode(newPassword);
        userAccount.changePassword(encodedPassword, OffsetDateTime.now());
        userAccountPort.save(userAccount);
    }
}
