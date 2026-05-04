package com.tmk.api.admin.account.result;

import com.tmk.core.admin.entity.AdminAccount;
import java.time.OffsetDateTime;

public record AdminAccountResult(
        Long adminId,
        String username,
        boolean active,
        OffsetDateTime createdAt
) {

    public static AdminAccountResult from(AdminAccount adminAccount) {
        return new AdminAccountResult(
                adminAccount.getId(),
                adminAccount.getUsername(),
                adminAccount.isActive(),
                adminAccount.getCreatedAt()
        );
    }
}
