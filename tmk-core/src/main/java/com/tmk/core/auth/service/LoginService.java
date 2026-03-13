package com.tmk.core.auth.service;

import com.tmk.core.port.out.PasswordEncoderPort;
import com.tmk.core.port.out.UserPort;
import com.tmk.core.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserPort userPort;
    private final PasswordEncoderPort passwordEncoder;

    public User login(String email, String rawPassword) {
        // TODO
        return null;
    }
}
