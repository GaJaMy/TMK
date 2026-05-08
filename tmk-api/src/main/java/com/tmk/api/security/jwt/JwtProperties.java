package com.tmk.api.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private TokenProperties admin = new TokenProperties();
    private TokenProperties user = new TokenProperties();

    @Getter
    @Setter
    public static class TokenProperties {
        private String secret;
        private long accessTokenExpiry;
        private long refreshTokenExpiry;
    }
}
