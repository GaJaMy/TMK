package com.tmk.api.admin.account.dto;

import com.tmk.api.admin.account.result.AdminAccountResult;
import java.time.OffsetDateTime;

public record AdminAccountSummaryResponse(
        Long adminId,
        String username,
        boolean active,
        OffsetDateTime createdAt
) {

    public static AdminAccountSummaryResponse from(AdminAccountResult result) {
        return new AdminAccountSummaryResponse(
                result.adminId(),
                result.username(),
                result.active(),
                result.createdAt()
        );
    }
}
