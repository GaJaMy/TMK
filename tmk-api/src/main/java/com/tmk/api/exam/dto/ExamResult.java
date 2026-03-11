package com.tmk.api.exam.dto;

public class ExamResult {

    private final Long examId;
    private final int totalQuestions;
    private final int timeLimit;
    private final String startedAt;
    private final String expiredAt;
    private final String status;

    public ExamResult(Long examId, int totalQuestions, int timeLimit,
                      String startedAt, String expiredAt, String status) {
        this.examId = examId;
        this.totalQuestions = totalQuestions;
        this.timeLimit = timeLimit;
        this.startedAt = startedAt;
        this.expiredAt = expiredAt;
        this.status = status;
    }

    public Long getExamId() { return examId; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getTimeLimit() { return timeLimit; }
    public String getStartedAt() { return startedAt; }
    public String getExpiredAt() { return expiredAt; }
    public String getStatus() { return status; }
}
