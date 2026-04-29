package com.tmk.api.admin.account.usecase;

import com.tmk.api.admin.account.dto.AdminAccountSummaryResponse;
import com.tmk.core.admin.entity.AdminAccount;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.AdminAccountPort;
import com.tmk.core.port.out.security.PasswordEncoderPort;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountUseCase {

    private final AdminAccountPort adminAccountPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public List<AdminAccountSummaryResponse> getAdminAccounts() {
        return adminAccountPort.findAll().stream()
                .map(adminAccount -> new AdminAccountSummaryResponse(
                        adminAccount.getId(),
                        adminAccount.getUsername(),
                        adminAccount.isActive(),
                        adminAccount.getCreatedAt()
                ))
                .toList();
    }

    public AdminAccountSummaryResponse createAdminAccount(Long createdByAdminId, String username, String password) {
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

        return new AdminAccountSummaryResponse(
                savedAdminAccount.getId(),
                savedAdminAccount.getUsername(),
                savedAdminAccount.isActive(),
                savedAdminAccount.getCreatedAt()
        );
    }

    public AdminAccountSummaryResponse changeAdminAccountStatus(Long adminId, boolean active) {
        AdminAccount adminAccount = adminAccountPort.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        OffsetDateTime now = OffsetDateTime.now();
        if (active) {
            adminAccount.activate(now);
        } else {
            adminAccount.deactivate(now);
        }

        AdminAccount savedAdminAccount = adminAccountPort.save(adminAccount);
        return new AdminAccountSummaryResponse(
                savedAdminAccount.getId(),
                savedAdminAccount.getUsername(),
                savedAdminAccount.isActive(),
                savedAdminAccount.getCreatedAt()
        );
    }

    public void deleteAdminAccount(Long adminId) {
        adminAccountPort.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        adminAccountPort.deleteById(adminId);
    }
}
