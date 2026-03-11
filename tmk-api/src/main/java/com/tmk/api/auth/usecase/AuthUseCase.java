package com.tmk.api.auth.usecase;

import com.tmk.api.auth.dto.LoginResult;
import com.tmk.api.auth.dto.ReissueResult;
import com.tmk.api.auth.dto.SocialLoginResult;
import com.tmk.core.auth.service.*;
import lombok.RequiredArgsConstructor;
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

    public void sendVerification(String email) {
        sendEmailVerificationService.sendVerification(email);
    }

    public void verify(String email, String code) {
        verifyEmailService.verify(email, code);
    }

    public void register(String email, String rawPassword) {
        registerUserService.register(email, rawPassword);
        // TODO: return result
    }

    public LoginResult login(String email, String rawPassword) {
        loginService.login(email, rawPassword);
        // TODO: build LoginResult from user + token
        return null;
    }

    public void logout(String accessToken) {
        logoutService.logout(accessToken);
    }

    public ReissueResult reissue(String refreshToken) {
        reissueTokenService.reissue(refreshToken);
        // TODO: build ReissueResult
        return null;
    }

    public SocialLoginResult socialLogin(String provider, String code) {
        socialLoginService.socialLogin(provider, code);
        // TODO: build SocialLoginResult
        return null;
    }
}
