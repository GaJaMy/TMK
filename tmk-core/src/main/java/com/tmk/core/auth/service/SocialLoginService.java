package com.tmk.core.auth.service;

import com.tmk.core.user.entity.User;
import com.tmk.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final UserRepository userRepository;

    public User socialLogin(String provider, String code) {
        // TODO
        return null;
    }
}
