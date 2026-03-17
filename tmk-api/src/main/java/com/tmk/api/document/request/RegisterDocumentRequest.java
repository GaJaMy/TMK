package com.tmk.api.document.request;

public class RegisterDocumentRequest {
    private String title;
    private String source;

    public RegisterDocumentRequest() {}

    public RegisterDocumentRequest(String title, String source) {
        this.title = title;
        this.source = source;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
