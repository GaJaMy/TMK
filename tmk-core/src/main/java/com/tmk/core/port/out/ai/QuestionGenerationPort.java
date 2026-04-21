package com.tmk.core.port.out.ai;

import com.tmk.core.document.vo.GeneratedQuestion;

import java.util.List;

public interface QuestionGenerationPort {
    List<GeneratedQuestion> generateQuestions(Long documentId, String topic, String context);
}
