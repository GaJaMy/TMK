package com.tmk.api.exam.dto;

public class ExamResultData {

    private final Long examId;
    private final int totalQuestions;
    private final int correctCount;
    private final double score;
    private final boolean passed;
    private final String submittedAt;

    public ExamResultData(Long examId, int totalQuestions, int correctCount,
                          double score, boolean passed, String submittedAt) {
        this.examId = examId;
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.score = score;
        this.passed = passed;
        this.submittedAt = submittedAt;
    }

    public Long getExamId() { return examId; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectCount() { return correctCount; }
    public double getScore() { return score; }
    public boolean isPassed() { return passed; }
    public String getSubmittedAt() { return submittedAt; }
}
