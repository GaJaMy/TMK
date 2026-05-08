package com.tmk.api.admin.account.service;

import com.tmk.api.admin.account.result.AdminAccountResult;
import com.tmk.core.admin.entity.AdminAccount;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.AdminAccountPort;
import com.tmk.core.port.out.security.PasswordEncoderPort;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountService {

    private final AdminAccountPort adminAccountPort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Transactional(readOnly = true)
    public List<AdminAccountResult> getAdminAccounts() {
        List<AdminAccount> adminAccounts = adminAccountPort.findAll();
        return adminAccounts.stream()
                .map(AdminAccountResult::from)
                .toList();
    }

    @Transactional
    public AdminAccountResult createAdminAccount(Long createdByAdminId, String username, String password) {
        if (adminAccountPort.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        OffsetDateTime now = OffsetDateTime.now();
        AdminAccount savedAdminAccount = adminAccountPort.save(
                AdminAccount.create(
                        username,
                        passwordEncoderPort.encode(password),
                        createdByAdminId,
                        now
                )
        );

        return AdminAccountResult.from(savedAdminAccount);
    }

    @Transactional
    public AdminAccountResult changeAdminAccountStatus(Long adminId, boolean active) {
        AdminAccount adminAccount = adminAccountPort.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        OffsetDateTime now = OffsetDateTime.now();
        if (active) {
            adminAccount.activate(now);
        } else {
            adminAccount.deactivate(now);
        }

        return AdminAccountResult.from(adminAccountPort.save(adminAccount));
    }

    @Transactional
    public void deleteAdminAccount(Long adminId) {
        adminAccountPort.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        adminAccountPort.deleteById(adminId);
    }
}
