package com.tmk.api.user.auth.usecase;

import com.tmk.api.user.auth.dto.LoginResponse;
import com.tmk.api.user.auth.dto.RegisterResponse;
import com.tmk.api.user.auth.request.LoginRequest;
import com.tmk.api.user.auth.request.RegisterRequest;
import com.tmk.api.user.auth.request.ReissueRequest;
import com.tmk.api.user.auth.request.ResetPasswordRequest;
import com.tmk.api.user.auth.result.LoginResult;
import com.tmk.api.user.auth.result.RegisterResult;
import com.tmk.api.user.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthUseCase {

    private final AuthService authService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        LoginResult result = authService.login(
                request.username(),
                request.password()
        );
        return LoginResponse.from(result);
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        RegisterResult result = authService.register(
                request.username(),
                request.password(),
                request.countryCode()
        );
        return RegisterResponse.from(result);
    }

    @Transactional
    public LoginResponse reissue(ReissueRequest request) {
        LoginResult result = authService.reissue(request.refreshToken());
        return LoginResponse.from(result);
    }

    @Transactional
    public void logout(Long principalId, String accessToken) {
        authService.logout(principalId, accessToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        authService.resetPassword(
                request.username(),
                request.newPassword()
        );
    }
}
