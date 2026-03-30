package com.tmk.core.port.out;

import com.tmk.core.document.vo.GeneratedQuestion;

import java.util.List;

public interface QuestionGenerationPort {
    List<GeneratedQuestion> generateQuestions(Long documentId, String context);
}
