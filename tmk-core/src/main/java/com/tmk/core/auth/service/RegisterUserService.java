package com.tmk.core.auth.service;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.PasswordEncoderPort;
import com.tmk.core.port.out.UserPort;
import com.tmk.core.user.entity.Provider;
import com.tmk.core.user.entity.User;
import com.tmk.core.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RegisterUserService {

    private final UserPort userPort;
    private final PasswordEncoderPort passwordEncoder;

    public User register(String email, String rawPassword) {
        if (userPort.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .provider(Provider.LOCAL)
                .role(UserRole.USER)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        return userPort.save(user);
    }
}
