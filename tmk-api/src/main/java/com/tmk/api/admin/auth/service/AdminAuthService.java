package com.tmk.api.admin.auth.service;

import com.tmk.api.admin.auth.result.AdminLoginResult;
import com.tmk.api.security.AdminAuthenticationProvider;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.security.jwt.JwtProvider;
import com.tmk.core.admin.entity.AdminAccount;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.cache.RefreshTokenPort;
import com.tmk.core.port.out.cache.TokenBlacklistPort;
import com.tmk.core.port.out.persistence.AdminAccountPort;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthService {

    private final AdminAccountPort adminAccountPort;
    private final AdminAuthenticationProvider adminAuthenticationProvider;
    private final JwtProvider jwtProvider;
    private final RefreshTokenPort refreshTokenPort;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Transactional
    public AdminLoginResult login(String username, String password) {
        Authentication authentication;
        try {
            authentication = adminAuthenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (DisabledException e) {
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_INACTIVE);
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }

        AuthenticatedPrincipal userDetails = (AuthenticatedPrincipal) authentication.getPrincipal();

        String accessToken = jwtProvider.generateAccessToken(
                userDetails.getPrincipalId(),
                userDetails.getUsername(),
                userDetails.getRole(),
                AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE
        );
        String refreshToken = jwtProvider.generateRefreshToken(
                userDetails.getPrincipalId(),
                AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE
        );

        refreshTokenPort.save(
                AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE,
                userDetails.getPrincipalId(),
                refreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiry(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE))
        );

        return new AdminLoginResult(
                userDetails.getPrincipalId(),
                userDetails.getUsername(),
                accessToken,
                refreshToken,
                jwtProvider.getAccessTokenExpiry(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE),
                userDetails.getRole()
        );
    }

    @Transactional
    public AdminLoginResult reissue(String refreshToken) {
        Claims claims;
        try {
            claims = jwtProvider.parseClaims(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String principalType = claims.get("principalType", String.class);
        if (!AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE.equals(principalType)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long principalId = Long.valueOf(claims.getSubject());
        String savedRefreshToken = refreshTokenPort.find(principalType, principalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!savedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        AdminAccount adminAccount = adminAccountPort.findById(principalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        if (!adminAccount.isActive()) {
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_INACTIVE);
        }

        String newAccessToken = jwtProvider.generateAccessToken(
                adminAccount.getId(),
                adminAccount.getUsername(),
                AuthenticatedPrincipal.ADMIN_ROLE,
                AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE
        );
        String newRefreshToken = jwtProvider.generateRefreshToken(
                adminAccount.getId(),
                AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE
        );

        refreshTokenPort.save(
                AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE,
                adminAccount.getId(),
                newRefreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiry(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE))
        );

        return new AdminLoginResult(
                adminAccount.getId(),
                adminAccount.getUsername(),
                newAccessToken,
                newRefreshToken,
                jwtProvider.getAccessTokenExpiry(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE),
                AuthenticatedPrincipal.ADMIN_ROLE
        );
    }

    @Transactional
    public void logout(Long principalId, String accessToken) {
        refreshTokenPort.delete(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE, principalId);
        Duration remainingValidity = jwtProvider.getRemainingValidity(accessToken);
        tokenBlacklistPort.blacklist(accessToken, remainingValidity);
    }
}
