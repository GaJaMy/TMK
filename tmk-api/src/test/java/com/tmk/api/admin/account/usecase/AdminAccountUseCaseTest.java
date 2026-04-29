package com.tmk.api.admin.account.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.tmk.api.admin.account.dto.AdminAccountSummaryResponse;
import com.tmk.core.admin.entity.AdminAccount;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.AdminAccountPort;
import com.tmk.core.port.out.security.PasswordEncoderPort;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminAccountUseCaseTest {

    @Mock
    private AdminAccountPort adminAccountPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private AdminAccountUseCase adminAccountUseCase;

    @Test
    void getAdminAccountsReturnsMappedAdminList() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        AdminAccount firstAdmin = AdminAccount.builder()
                .id(101L)
                .username("admin-master")
                .password("encoded-password")
                .active(true)
                .createdByAdminId(null)
                .createdAt(now)
                .updatedAt(now)
                .build();
        AdminAccount secondAdmin = AdminAccount.builder()
                .id(100L)
                .username("admin-sub")
                .password("encoded-password")
                .active(false)
                .createdByAdminId(101L)
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusDays(1))
                .build();

        given(adminAccountPort.findAll()).willReturn(List.of(firstAdmin, secondAdmin));

        List<AdminAccountSummaryResponse> result = adminAccountUseCase.getAdminAccounts();

        assertThat(result).containsExactly(
                new AdminAccountSummaryResponse(101L, "admin-master", true, now),
                new AdminAccountSummaryResponse(100L, "admin-sub", false, now.minusDays(1))
        );
    }

    @Test
    void createAdminAccountSavesEncodedAdminAccount() {
        OffsetDateTime now = OffsetDateTime.now();
        AdminAccount savedAdmin = AdminAccount.builder()
                .id(102L)
                .username("admin2")
                .password("encoded-password")
                .active(true)
                .createdByAdminId(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(adminAccountPort.existsByUsername("admin2")).willReturn(false);
        given(passwordEncoderPort.encode("Password1234!")).willReturn("encoded-password");
        given(adminAccountPort.save(org.mockito.ArgumentMatchers.any(AdminAccount.class))).willReturn(savedAdmin);

        AdminAccountSummaryResponse result = adminAccountUseCase.createAdminAccount(101L, "admin2", "Password1234!");

        assertThat(result).isEqualTo(new AdminAccountSummaryResponse(102L, "admin2", true, now));
        then(adminAccountPort).should().existsByUsername("admin2");
        then(passwordEncoderPort).should().encode("Password1234!");
        then(adminAccountPort).should().save(org.mockito.ArgumentMatchers.any(AdminAccount.class));
    }

    @Test
    void createAdminAccountThrowsWhenUsernameAlreadyExists() {
        given(adminAccountPort.existsByUsername("admin2")).willReturn(true);

        assertThatThrownBy(() -> adminAccountUseCase.createAdminAccount(101L, "admin2", "Password1234!"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DUPLICATE_USERNAME.getMessage());
    }

    @Test
    void changeAdminAccountStatusDeactivatesAdminAccount() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        AdminAccount adminAccount = AdminAccount.builder()
                .id(101L)
                .username("admin-master")
                .password("encoded-password")
                .active(true)
                .createdByAdminId(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(adminAccountPort.findById(101L)).willReturn(java.util.Optional.of(adminAccount));
        given(adminAccountPort.save(org.mockito.ArgumentMatchers.any(AdminAccount.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        AdminAccountSummaryResponse result = adminAccountUseCase.changeAdminAccountStatus(101L, false);

        assertThat(result.adminId()).isEqualTo(101L);
        assertThat(result.active()).isFalse();

        ArgumentCaptor<AdminAccount> captor = ArgumentCaptor.forClass(AdminAccount.class);
        then(adminAccountPort).should().save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void changeAdminAccountStatusThrowsWhenAdminAccountDoesNotExist() {
        given(adminAccountPort.findById(999L)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> adminAccountUseCase.changeAdminAccountStatus(999L, false))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ADMIN_NOT_FOUND.getMessage());
    }

    @Test
    void deleteAdminAccountDeletesWhenAdminAccountExists() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        AdminAccount adminAccount = AdminAccount.builder()
                .id(101L)
                .username("admin-master")
                .password("encoded-password")
                .active(true)
                .createdByAdminId(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(adminAccountPort.findById(101L)).willReturn(java.util.Optional.of(adminAccount));

        adminAccountUseCase.deleteAdminAccount(101L);

        then(adminAccountPort).should().deleteById(101L);
    }

    @Test
    void deleteAdminAccountThrowsWhenAdminAccountDoesNotExist() {
        given(adminAccountPort.findById(999L)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> adminAccountUseCase.deleteAdminAccount(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ADMIN_NOT_FOUND.getMessage());
    }
}
