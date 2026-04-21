package com.tmk.api.question.dto;

public class QuestionSummary {

    private final Long questionId;
    private final String content;
    private final String type;
    private final String difficulty;
    private final String topic;
    private final String scope;
    private final String sourceType;

    public QuestionSummary(Long questionId, String content, String type, String difficulty, String topic, String scope, String sourceType) {
        this.questionId = questionId;
        this.content = content;
        this.type = type;
        this.difficulty = difficulty;
        this.topic = topic;
        this.scope = scope;
        this.sourceType = sourceType;
    }

    public Long getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getDifficulty() { return difficulty; }
    public String getTopic() { return topic; }
    public String getScope() { return scope; }
    public String getSourceType() { return sourceType; }
}
