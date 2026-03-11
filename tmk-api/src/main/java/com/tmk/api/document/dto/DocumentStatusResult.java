package com.tmk.api.document.dto;

public class DocumentStatusResult {

    private final Long documentId;
    private final String title;
    private final String status;
    private final int chunkCount;
    private final int questionCount;
    private final String createdAt;

    public DocumentStatusResult(Long documentId, String title, String status,
                                int chunkCount, int questionCount, String createdAt) {
        this.documentId = documentId;
        this.title = title;
        this.status = status;
        this.chunkCount = chunkCount;
        this.questionCount = questionCount;
        this.createdAt = createdAt;
    }

    public Long getDocumentId() { return documentId; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public int getChunkCount() { return chunkCount; }
    public int getQuestionCount() { return questionCount; }
    public String getCreatedAt() { return createdAt; }
}
