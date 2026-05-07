package com.tmk.api.user.exam.result;

import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import java.util.List;

public record ExamQuestionDetailResult(
        Long examQuestionId,
        short orderNum,
        String content,
        QuestionType type,
        Difficulty difficulty,
        List<ExamQuestionOptionResult> options,
        String myAnswer
) {
}
