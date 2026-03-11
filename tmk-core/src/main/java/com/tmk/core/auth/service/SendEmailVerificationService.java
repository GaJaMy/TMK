package com.tmk.core.auth.service;

import com.tmk.core.emailverification.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendEmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;

    public void sendVerification(String email) {
        // TODO
    }
}
