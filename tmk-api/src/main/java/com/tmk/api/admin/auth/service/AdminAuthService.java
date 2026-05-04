package com.tmk.api.admin.auth.service;

import com.tmk.api.admin.auth.result.AdminLoginResult;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.security.jwt.JwtProvider;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.cache.RefreshTokenPort;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
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

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenPort refreshTokenPort;

    @Transactional
    public AdminLoginResult login(String username, String password) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
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
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiry())
        );

        return new AdminLoginResult(
                userDetails.getPrincipalId(),
                userDetails.getUsername(),
                accessToken,
                refreshToken,
                jwtProvider.getAccessTokenExpiry()
        );
    }
}
