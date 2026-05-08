package com.tmk.core.question.vo;

import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import java.util.List;

public record PrivateQuestionDraft(
        String content,
        QuestionType type,
        Difficulty difficulty,
        String answer,
        String explanation,
        List<String> options
) {
}
