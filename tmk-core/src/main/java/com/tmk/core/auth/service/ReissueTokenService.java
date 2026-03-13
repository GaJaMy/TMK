package com.tmk.core.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReissueTokenService {

    public String reissue(String refreshToken) {
        // Redis validation is handled in AuthUseCase
        return null;
    }
}
