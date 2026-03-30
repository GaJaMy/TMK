package com.tmk.core.document.vo;

import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;

import java.util.List;

public record GeneratedQuestion(
    String content,
    QuestionType type,
    Difficulty difficulty,
    String answer,
    String explanation,
    List<String> options
) {}
