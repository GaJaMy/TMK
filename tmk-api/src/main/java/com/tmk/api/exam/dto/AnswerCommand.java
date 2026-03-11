package com.tmk.api.exam.dto;

public class AnswerCommand {

    private final Long questionId;
    private final String answer;

    public AnswerCommand(Long questionId, String answer) {
        this.questionId = questionId;
        this.answer = answer;
    }

    public Long getQuestionId() { return questionId; }
    public String getAnswer() { return answer; }
}
