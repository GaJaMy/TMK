package com.tmk.api.user.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.security.UserAuthenticationProvider;
import com.tmk.api.security.jwt.JwtProvider;
import com.tmk.api.user.auth.result.LoginResult;
import com.tmk.api.user.auth.result.RegisterResult;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.cache.RefreshTokenPort;
import com.tmk.core.port.out.cache.TokenBlacklistPort;
import com.tmk.core.port.out.persistence.UserAccountPort;
import com.tmk.core.port.out.security.PasswordEncoderPort;
import com.tmk.core.user.entity.UserAccount;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountPort userAccountPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private UserAuthenticationProvider userAuthenticationProvider;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @Mock
    private TokenBlacklistPort tokenBlacklistPort;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerSavesEncodedUserAccount() {
        OffsetDateTime now = OffsetDateTime.now();
        UserAccount savedUserAccount = UserAccount.builder()
                .id(1L)
                .username("howard")
                .password("encoded-password")
                .active(true)
                .countryCode("KR")
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(userAccountPort.existsByUsername("howard")).willReturn(false);
        given(passwordEncoderPort.encode("Password1234!")).willReturn("encoded-password");
        given(userAccountPort.save(any(UserAccount.class))).willReturn(savedUserAccount);

        RegisterResult result = authService.register("howard", "Password1234!", "kr");

        assertThat(result).isEqualTo(new RegisterResult(1L, "howard"));
        then(userAccountPort).should().existsByUsername("howard");
        then(passwordEncoderPort).should().encode("Password1234!");
        then(userAccountPort).should().save(any(UserAccount.class));
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {
        given(userAccountPort.existsByUsername("howard")).willReturn(true);

        assertThatThrownBy(() -> authService.register("howard", "Password1234!", "KR"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DUPLICATE_USERNAME.getMessage());
    }

    @Test
    void loginReturnsUserTokens() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                "howard",
                null,
                1L,
                AuthenticatedPrincipal.USER_ROLE,
                AuthenticatedPrincipal.USER_PRINCIPAL_TYPE
        );
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        given(userAuthenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtProvider.generateAccessToken(1L, "howard", AuthenticatedPrincipal.USER_ROLE, AuthenticatedPrincipal.USER_PRINCIPAL_TYPE))
                .willReturn("user-access-token");
        given(jwtProvider.generateRefreshToken(1L, AuthenticatedPrincipal.USER_PRINCIPAL_TYPE))
                .willReturn("user-refresh-token");
        given(jwtProvider.getRefreshTokenExpiry(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE))
                .willReturn(604800000L);
        given(jwtProvider.getAccessTokenExpiry(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE))
                .willReturn(1800000L);

        LoginResult result = authService.login("howard", "Password1234!");

        assertThat(result).isEqualTo(new LoginResult(
                1L,
                "howard",
                "user-access-token",
                "user-refresh-token",
                1800000L,
                AuthenticatedPrincipal.USER_ROLE
        ));
        then(refreshTokenPort).should().save(
                AuthenticatedPrincipal.USER_PRINCIPAL_TYPE,
                1L,
                "user-refresh-token",
                java.time.Duration.ofMillis(604800000L)
        );
    }

    @Test
    void loginThrowsWhenUserAccountInactive() {
        given(userAuthenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new DisabledException("비활성 사용자 계정입니다."));

        assertThatThrownBy(() -> authService.login("howard", "Password1234!"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.USER_ACCOUNT_INACTIVE.getMessage());
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() {
        given(userAuthenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login("howard", "Password1234!"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_USERNAME_OR_PASSWORD.getMessage());
    }

    @Test
    void reissueReturnsNewTokensWhenRefreshTokenMatches() {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        given(claims.getSubject()).willReturn("1");
        given(claims.get("principalType", String.class)).willReturn(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE);

        OffsetDateTime now = OffsetDateTime.now();
        UserAccount userAccount = UserAccount.builder()
                .id(1L)
                .username("howard")
                .password("encoded-password")
                .active(true)
                .countryCode("KR")
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(jwtProvider.parseClaims("refresh-token")).willReturn(claims);
        given(refreshTokenPort.find(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE, 1L))
                .willReturn(java.util.Optional.of("refresh-token"));
        given(userAccountPort.findById(1L)).willReturn(java.util.Optional.of(userAccount));
        given(jwtProvider.generateAccessToken(1L, "howard", AuthenticatedPrincipal.USER_ROLE, AuthenticatedPrincipal.USER_PRINCIPAL_TYPE))
                .willReturn("new-access-token");
        given(jwtProvider.generateRefreshToken(1L, AuthenticatedPrincipal.USER_PRINCIPAL_TYPE))
                .willReturn("new-refresh-token");
        given(jwtProvider.getRefreshTokenExpiry(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE)).willReturn(604800000L);
        given(jwtProvider.getAccessTokenExpiry(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE)).willReturn(1800000L);

        LoginResult result = authService.reissue("refresh-token");

        assertThat(result).isEqualTo(new LoginResult(
                1L,
                "howard",
                "new-access-token",
                "new-refresh-token",
                1800000L,
                AuthenticatedPrincipal.USER_ROLE
        ));
    }

    @Test
    void reissueThrowsWhenRefreshTokenDoesNotMatch() {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        given(claims.getSubject()).willReturn("1");
        given(claims.get("principalType", String.class)).willReturn(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE);

        given(jwtProvider.parseClaims("refresh-token")).willReturn(claims);
        given(refreshTokenPort.find(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE, 1L))
                .willReturn(java.util.Optional.of("another-token"));

        assertThatThrownBy(() -> authService.reissue("refresh-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
    }

    @Test
    void logoutDeletesRefreshTokenAndBlacklistsAccessToken() {
        given(jwtProvider.getRemainingValidity("access-token")).willReturn(Duration.ofMinutes(30));

        authService.logout(1L, "access-token");

        then(refreshTokenPort).should().delete(AuthenticatedPrincipal.USER_PRINCIPAL_TYPE, 1L);
        then(tokenBlacklistPort).should().blacklist("access-token", Duration.ofMinutes(30));
    }

    @Test
    void resetPasswordChangesEncodedPassword() {
        OffsetDateTime now = OffsetDateTime.now();
        UserAccount userAccount = UserAccount.builder()
                .id(1L)
                .username("howard")
                .password("old-password")
                .active(true)
                .countryCode("KR")
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(userAccountPort.findByUsername("howard")).willReturn(java.util.Optional.of(userAccount));
        given(passwordEncoderPort.encode("NewPassword1234!")).willReturn("encoded-new-password");
        given(userAccountPort.save(any(UserAccount.class))).willAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword("howard", "NewPassword1234!");

        assertThat(userAccount.getPassword()).isEqualTo("encoded-new-password");
        then(userAccountPort).should().save(userAccount);
    }

    @Test
    void resetPasswordThrowsWhenTargetDoesNotExist() {
        given(userAccountPort.findByUsername("howard")).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword("howard", "NewPassword1234!"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.RESET_PASSWORD_TARGET_NOT_FOUND.getMessage());
    }
}
