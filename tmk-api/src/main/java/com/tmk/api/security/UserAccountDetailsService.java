package com.tmk.api.security;

import com.tmk.core.port.out.persistence.UserAccountPort;
import com.tmk.core.user.entity.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccountDetailsService implements UserDetailsService {

    private final UserAccountPort userAccountPort;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountPort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        if (!userAccount.isActive()) {
            throw new DisabledException("비활성 사용자 계정입니다.");
        }

        return new AuthenticatedPrincipal(
                userAccount.getUsername(),
                userAccount.getPassword(),
                userAccount.getId(),
                AuthenticatedPrincipal.USER_ROLE,
                AuthenticatedPrincipal.USER_PRINCIPAL_TYPE
        );
    }
}
