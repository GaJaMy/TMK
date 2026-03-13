package com.tmk.core.auth.service;

import com.tmk.core.port.out.UserPort;
import com.tmk.core.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final UserPort userPort;

    public User socialLogin(String provider, String code) {
        throw new UnsupportedOperationException("Social login not implemented");
    }
}
