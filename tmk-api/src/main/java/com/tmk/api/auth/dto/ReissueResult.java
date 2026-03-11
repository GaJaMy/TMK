package com.tmk.api.auth.dto;

public class ReissueResult {

    private final String accessToken;
    private final long expiresIn;

    public ReissueResult(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() { return accessToken; }
    public long getExpiresIn() { return expiresIn; }
}
