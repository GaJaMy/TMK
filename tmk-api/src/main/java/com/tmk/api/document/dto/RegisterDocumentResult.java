package com.tmk.api.document.dto;

public class RegisterDocumentResult {

    private final Long documentId;
    private final String title;
    private final String topic;
    private final String scope;
    private final String status;
    private final String createdAt;

    public RegisterDocumentResult(Long documentId, String title, String topic, String scope, String status, String createdAt) {
        this.documentId = documentId;
        this.title = title;
        this.topic = topic;
        this.scope = scope;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getDocumentId() { return documentId; }
    public String getTitle() { return title; }
    public String getTopic() { return topic; }
    public String getScope() { return scope; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
