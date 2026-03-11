package com.tmk.core.auth.service;

import com.tmk.core.user.entity.User;
import com.tmk.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;

    public User login(String email, String rawPassword) {
        // TODO
        return null;
    }
}
