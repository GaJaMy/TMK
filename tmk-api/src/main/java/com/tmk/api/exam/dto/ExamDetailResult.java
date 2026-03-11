package com.tmk.api.exam.dto;

import java.util.List;

public class ExamDetailResult {

    private final Long examId;
    private final String status;
    private final String expiredAt;
    private final List<ExamQuestionResult> questions;

    public ExamDetailResult(Long examId, String status, String expiredAt,
                            List<ExamQuestionResult> questions) {
        this.examId = examId;
        this.status = status;
        this.expiredAt = expiredAt;
        this.questions = questions;
    }

    public Long getExamId() { return examId; }
    public String getStatus() { return status; }
    public String getExpiredAt() { return expiredAt; }
    public List<ExamQuestionResult> getQuestions() { return questions; }
}
