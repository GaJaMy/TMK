package com.tmk.api.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long principalId;
    private final String username;
    private final String password;
    private final String role;
    private final String principalType;

    public CustomUserDetails(String username, String password, Long principalId, String role, String principalType) {
        this.username = username;
        this.password = password;
        this.principalId = principalId;
        this.role = role;
        this.principalType = principalType;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public Long getUserId() {
        return principalId;
    }

    public Long getPrincipalId() {
        return principalId;
    }

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
}
