package com.tmk.core.auth.service;

import com.tmk.core.port.out.EmailVerificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendEmailVerificationService {

    private final EmailVerificationPort emailVerificationPort;

    public void sendVerification(String email) {
        // TODO
    }
}
