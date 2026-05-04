package com.tmk.api.admin.account.usecase;

import com.tmk.api.admin.account.dto.AdminAccountSummaryResponse;
import com.tmk.api.admin.account.request.AdminAccountCreateRequest;
import com.tmk.api.admin.account.request.AdminAccountStatusChangeRequest;
import com.tmk.api.admin.account.result.AdminAccountResult;
import com.tmk.api.admin.account.service.AdminAccountService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountUseCase {

    private final AdminAccountService adminAccountService;

    public List<AdminAccountSummaryResponse> getAdminAccounts() {
        List<AdminAccountResult> results = adminAccountService.getAdminAccounts();
        return results.stream()
                .map(AdminAccountSummaryResponse::from)
                .toList();
    }

    @Transactional
    public AdminAccountSummaryResponse createAdminAccount(Long createdByAdminId, AdminAccountCreateRequest request) {
        AdminAccountResult result = adminAccountService.createAdminAccount(
                createdByAdminId,
                request.username(),
                request.password()
        );
        return AdminAccountSummaryResponse.from(result);
    }

    @Transactional
    public AdminAccountSummaryResponse changeAdminAccountStatus(
            Long adminId,
            AdminAccountStatusChangeRequest request
    ) {
        AdminAccountResult result = adminAccountService.changeAdminAccountStatus(adminId, request.active());
        return AdminAccountSummaryResponse.from(result);
    }

    @Transactional
    public void deleteAdminAccount(Long adminId) {
        adminAccountService.deleteAdminAccount(adminId);
    }
}
