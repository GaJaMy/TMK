package com.tmk.api.auth.dto;

public class LoginResult {

    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;

    public LoginResult(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public long getExpiresIn() { return expiresIn; }
}
