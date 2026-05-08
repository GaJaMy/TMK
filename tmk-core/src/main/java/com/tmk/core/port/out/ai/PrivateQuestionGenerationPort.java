package com.tmk.core.port.out.ai;

import com.tmk.core.question.vo.PrivateQuestionDraft;
import java.util.List;

public interface PrivateQuestionGenerationPort {

    List<PrivateQuestionDraft> generateQuestions(
            String languageCode,
            String documentTitle,
            List<String> chunkContents
    );
}
