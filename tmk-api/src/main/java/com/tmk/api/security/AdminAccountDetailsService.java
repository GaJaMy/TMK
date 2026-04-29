package com.tmk.api.security;

import com.tmk.core.port.out.persistence.AdminAccountPort;
import com.tmk.core.admin.entity.AdminAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAccountDetailsService implements UserDetailsService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final AdminAccountPort adminAccountPort;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminAccount adminAccount = adminAccountPort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + username));

        if (!adminAccount.isActive()) {
            throw new DisabledException("비활성 관리자 계정입니다.");
        }

        return new AuthenticatedPrincipal(
                adminAccount.getUsername(),
                adminAccount.getPassword(),
                adminAccount.getId(),
                ADMIN_ROLE,
                AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE
        );
    }
}
