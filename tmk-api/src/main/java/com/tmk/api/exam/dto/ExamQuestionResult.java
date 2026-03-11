package com.tmk.api.exam.dto;

import com.tmk.api.question.dto.OptionResult;

import java.util.List;

public class ExamQuestionResult {

    private final Long questionId;
    private final int order;
    private final String content;
    private final String type;
    private final String difficulty;
    private final List<OptionResult> options;
    private final String myAnswer;

    public ExamQuestionResult(Long questionId, int order, String content, String type,
                              String difficulty, List<OptionResult> options, String myAnswer) {
        this.questionId = questionId;
        this.order = order;
        this.content = content;
        this.type = type;
        this.difficulty = difficulty;
        this.options = options;
        this.myAnswer = myAnswer;
    }

    public Long getQuestionId() { return questionId; }
    public int getOrder() { return order; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getDifficulty() { return difficulty; }
    public List<OptionResult> getOptions() { return options; }
    public String getMyAnswer() { return myAnswer; }
}
