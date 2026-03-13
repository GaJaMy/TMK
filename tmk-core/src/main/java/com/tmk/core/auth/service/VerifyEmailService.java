package com.tmk.core.auth.service;

import com.tmk.core.port.out.EmailVerificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifyEmailService {

    private final EmailVerificationPort emailVerificationPort;

    public void verify(String email, String code) {
        // TODO
    }
}
