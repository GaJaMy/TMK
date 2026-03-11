package com.tmk.api.exam.dto;

import java.util.List;

public class HistoryDetailResult {

    private final Long examId;
    private final int totalQuestions;
    private final int correctCount;
    private final double score;
    private final boolean passed;
    private final String submittedAt;
    private final List<HistoryQuestionResult> questions;

    public HistoryDetailResult(Long examId, int totalQuestions, int correctCount, double score,
                               boolean passed, String submittedAt, List<HistoryQuestionResult> questions) {
        this.examId = examId;
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.score = score;
        this.passed = passed;
        this.submittedAt = submittedAt;
        this.questions = questions;
    }

    public Long getExamId() { return examId; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectCount() { return correctCount; }
    public double getScore() { return score; }
    public boolean isPassed() { return passed; }
    public String getSubmittedAt() { return submittedAt; }
    public List<HistoryQuestionResult> getQuestions() { return questions; }
}
