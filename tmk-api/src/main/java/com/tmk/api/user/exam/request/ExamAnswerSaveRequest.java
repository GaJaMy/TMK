package com.tmk.api.user.exam.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExamAnswerSaveRequest(
        @NotNull Long questionId,
        @NotBlank String answer
) {
}
