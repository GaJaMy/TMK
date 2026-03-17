package com.tmk.api.auth.request;

public class SocialLoginRequest {
    private String code;

    public SocialLoginRequest() {}

    public SocialLoginRequest(String code) {
        this.code = code;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
