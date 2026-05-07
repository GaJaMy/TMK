package com.tmk.api.user.exam.command;

public record ExamAnswerSaveCommand(
        Long questionId,
        String answer
) {
}
