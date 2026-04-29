package com.tmk.api.admin.account.dto;

import java.time.OffsetDateTime;

public record AdminAccountSummaryResponse(
        Long adminId,
        String username,
        boolean active,
        OffsetDateTime createdAt
) {
}
