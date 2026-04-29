package com.tmk.api.admin.account.controller;

import com.tmk.api.admin.account.dto.AdminAccountSummaryResponse;
import com.tmk.api.admin.account.request.AdminAccountCreateRequest;
import com.tmk.api.admin.account.request.AdminAccountStatusChangeRequest;
import com.tmk.api.admin.account.usecase.AdminAccountUseCase;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.common.ApiResponse;
import com.tmk.api.common.ApiVersion;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAccountController implements AdminAccountControllerDocs {

    private final AdminAccountUseCase adminAccountUseCase;

    @GetMapping(ApiVersion.V1 + "/users")
    @Override
    public ResponseEntity<ApiResponse<List<AdminAccountSummaryResponse>>> getAdminAccounts() {
        return ApiResponse.ok(adminAccountUseCase.getAdminAccounts());
    }

    @PostMapping(ApiVersion.V1 + "/users")
    @Override
    public ResponseEntity<ApiResponse<AdminAccountSummaryResponse>> createAdminAccount(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AdminAccountCreateRequest request
    ) {
        AdminAccountSummaryResponse response = adminAccountUseCase.createAdminAccount(
                principal.getPrincipalId(),
                request.username(),
                request.password()
        );
        return ApiResponse.ok(response);
    }

    @PatchMapping(ApiVersion.V1 + "/users/{userId}/status")
    @Override
    public ResponseEntity<ApiResponse<AdminAccountSummaryResponse>> changeAdminAccountStatus(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody AdminAccountStatusChangeRequest request
    ) {
        return ApiResponse.ok(adminAccountUseCase.changeAdminAccountStatus(userId, request.active()));
    }

    @DeleteMapping(ApiVersion.V1 + "/users/{userId}")
    @Override
    public ResponseEntity<ApiResponse<Void>> deleteAdminAccount(
            @PathVariable("userId") Long userId
    ) {
        adminAccountUseCase.deleteAdminAccount(userId);
        return ApiResponse.noContent();
    }
}
