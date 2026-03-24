package com.tmk.core.auth.service;

import com.tmk.core.port.out.PasswordEncoderPort;
import com.tmk.core.port.out.UserPort;
import com.tmk.core.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserService {

    private final UserPort userPort;
    private final PasswordEncoderPort passwordEncoder;

    public User register(String email, String rawPassword) {
        User user = User.create(email, passwordEncoder.encode(rawPassword));

        return userPort.save(user);
    }
}
