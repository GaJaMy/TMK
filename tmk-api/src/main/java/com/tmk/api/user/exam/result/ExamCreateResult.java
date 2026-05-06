package com.tmk.api.user.exam.result;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamStatus;

public record ExamCreateResult(
        Long examId,
        ExamSourceType sourceType,
        short totalQuestions,
        short timeLimitMinutes,
        ExamStatus status
) {

    public static ExamCreateResult from(Exam exam) {
        return new ExamCreateResult(
                exam.getId(),
                exam.getSourceType(),
                exam.getTotalQuestions(),
                exam.getTimeLimitMinutes(),
                exam.getStatus()
        );
    }
}
