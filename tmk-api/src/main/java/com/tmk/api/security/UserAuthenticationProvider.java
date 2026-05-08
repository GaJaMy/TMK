package com.tmk.api.security;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticationProvider extends DaoAuthenticationProvider {

    public UserAuthenticationProvider(
            UserAccountDetailsService userAccountDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        super(userAccountDetailsService);
        setPasswordEncoder(passwordEncoder);
    }
}
