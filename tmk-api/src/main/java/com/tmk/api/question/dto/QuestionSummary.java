package com.tmk.api.question.dto;

public class QuestionSummary {

    private final Long questionId;
    private final String content;
    private final String type;
    private final String difficulty;

    public QuestionSummary(Long questionId, String content, String type, String difficulty) {
        this.questionId = questionId;
        this.content = content;
        this.type = type;
        this.difficulty = difficulty;
    }

    public Long getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getDifficulty() { return difficulty; }
}
