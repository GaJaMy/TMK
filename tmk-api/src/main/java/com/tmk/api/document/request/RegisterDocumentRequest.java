package com.tmk.api.document.request;

public class RegisterDocumentRequest {
    private String title;
    private String source;
    private String topic;

    public RegisterDocumentRequest() {}

    public RegisterDocumentRequest(String title, String source, String topic) {
        this.title = title;
        this.source = source;
        this.topic = topic;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}
