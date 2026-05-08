package com.tmk.api.admin.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private AdminAccountPort adminAccountPort;

    @Mock
    private AdminAuthenticationProvider adminAuthenticationProvider;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @Mock
    private TokenBlacklistPort tokenBlacklistPort;

    @InjectMocks
    private AdminAuthService adminAuthService;

    @Test
    void loginReturnsAdminTokens() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                "admin",
                null,
                1L,
                AuthenticatedPrincipal.ADMIN_ROLE,
                AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE
        );
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        given(adminAuthenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtProvider.generateAccessToken(1L, "admin", AuthenticatedPrincipal.ADMIN_ROLE, AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE))
                .willReturn("admin-access-token");
        given(jwtProvider.generateRefreshToken(1L, AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE))
                .willReturn("admin-refresh-token");
        given(jwtProvider.getRefreshTokenExpiry(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE))
                .willReturn(604800000L);
        given(jwtProvider.getAccessTokenExpiry(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE))
                .willReturn(1800000L);

        AdminLoginResult result = adminAuthService.login("admin", "Password1234!");

        assertThat(result).isEqualTo(new AdminLoginResult(
                1L,
                "admin",
                "admin-access-token",
                "admin-refresh-token",
                1800000L,
                AuthenticatedPrincipal.ADMIN_ROLE
        ));
        then(refreshTokenPort).should().save(
                AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE,
                1L,
                "admin-refresh-token",
                Duration.ofMillis(604800000L)
        );
    }

    @Test
    void loginThrowsWhenAdminAccountInactive() {
        given(adminAuthenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new DisabledException("비활성 관리자 계정입니다."));

        assertThatThrownBy(() -> adminAuthService.login("admin", "Password1234!"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ADMIN_ACCOUNT_INACTIVE.getMessage());
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() {
        given(adminAuthenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> adminAuthService.login("admin", "Password1234!"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_USERNAME_OR_PASSWORD.getMessage());
    }

    @Test
    void reissueReturnsNewTokensWhenRefreshTokenMatches() {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        given(claims.getSubject()).willReturn("1");
        given(claims.get("principalType", String.class)).willReturn(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE);

        OffsetDateTime now = OffsetDateTime.now();
        AdminAccount adminAccount = AdminAccount.builder()
                .id(1L)
                .username("admin")
                .password("encoded-password")
                .active(true)
                .createdByAdminId(1L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(jwtProvider.parseClaims("refresh-token")).willReturn(claims);
        given(refreshTokenPort.find(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE, 1L))
                .willReturn(java.util.Optional.of("refresh-token"));
        given(adminAccountPort.findById(1L)).willReturn(java.util.Optional.of(adminAccount));
        given(jwtProvider.generateAccessToken(1L, "admin", AuthenticatedPrincipal.ADMIN_ROLE, AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE))
                .willReturn("new-access-token");
        given(jwtProvider.generateRefreshToken(1L, AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE))
                .willReturn("new-refresh-token");
        given(jwtProvider.getRefreshTokenExpiry(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE)).willReturn(604800000L);
        given(jwtProvider.getAccessTokenExpiry(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE)).willReturn(1800000L);

        AdminLoginResult result = adminAuthService.reissue("refresh-token");

        assertThat(result).isEqualTo(new AdminLoginResult(
                1L,
                "admin",
                "new-access-token",
                "new-refresh-token",
                1800000L,
                AuthenticatedPrincipal.ADMIN_ROLE
        ));
    }

    @Test
    void reissueThrowsWhenRefreshTokenDoesNotMatch() {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        given(claims.getSubject()).willReturn("1");
        given(claims.get("principalType", String.class)).willReturn(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE);

        given(jwtProvider.parseClaims("refresh-token")).willReturn(claims);
        given(refreshTokenPort.find(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE, 1L))
                .willReturn(java.util.Optional.of("another-token"));

        assertThatThrownBy(() -> adminAuthService.reissue("refresh-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
    }

    @Test
    void logoutDeletesRefreshTokenAndBlacklistsAccessToken() {
        given(jwtProvider.getRemainingValidity("access-token")).willReturn(Duration.ofMinutes(30));

        adminAuthService.logout(1L, "access-token");

        then(refreshTokenPort).should().delete(AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE, 1L);
        then(tokenBlacklistPort).should().blacklist("access-token", Duration.ofMinutes(30));
    }
}
