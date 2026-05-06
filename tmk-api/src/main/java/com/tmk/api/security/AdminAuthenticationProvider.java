package com.tmk.api.security;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthenticationProvider extends DaoAuthenticationProvider {

    public AdminAuthenticationProvider(
            AdminAccountDetailsService adminAccountDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        super(adminAccountDetailsService);
        setPasswordEncoder(passwordEncoder);
    }
}
