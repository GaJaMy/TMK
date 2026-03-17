package com.tmk.api.auth.request;

public class SendVerificationRequest {
    private String email;

    public SendVerificationRequest() {}

    public SendVerificationRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
