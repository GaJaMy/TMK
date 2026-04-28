package com.tmk.core.admin.service;

import com.tmk.core.admin.entity.AdminAccount;
import com.tmk.core.admin.vo.AdminLoginResult;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.AdminAccountPort;
import com.tmk.core.port.out.security.PasswordEncoderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminLoginService {

    private final AdminAccountPort adminAccountPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public AdminLoginResult login(String username, String rawPassword) {
        AdminAccount adminAccount = adminAccountPort.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USERNAME_OR_PASSWORD));

        if (!adminAccount.isActive()) {
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_INACTIVE);
        }

        if (!passwordEncoderPort.matches(rawPassword, adminAccount.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }

        return new AdminLoginResult(adminAccount.getId(), adminAccount.getUsername(), "ADMIN");
    }
}
