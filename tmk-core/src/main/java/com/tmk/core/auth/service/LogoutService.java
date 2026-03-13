package com.tmk.core.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    public void logout(String accessToken) {
        // Redis blacklist registration is handled in AuthUseCase
    }
}
