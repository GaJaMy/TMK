package com.tmk.api.auth.request;

public class ReissueRequest {
    private String refreshToken;

    public ReissueRequest() {}

    public ReissueRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
