package com.tmk.api.question.dto;

import java.util.List;

public class QuestionDetailResult {

    private final Long questionId;
    private final String content;
    private final String type;
    private final String difficulty;
    private final List<OptionResult> options;
    private final String answer;
    private final String explanation;

    public QuestionDetailResult(Long questionId, String content, String type, String difficulty,
                                List<OptionResult> options, String answer, String explanation) {
        this.questionId = questionId;
        this.content = content;
        this.type = type;
        this.difficulty = difficulty;
        this.options = options;
        this.answer = answer;
        this.explanation = explanation;
    }

    public Long getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getDifficulty() { return difficulty; }
    public List<OptionResult> getOptions() { return options; }
    public String getAnswer() { return answer; }
    public String getExplanation() { return explanation; }
}
