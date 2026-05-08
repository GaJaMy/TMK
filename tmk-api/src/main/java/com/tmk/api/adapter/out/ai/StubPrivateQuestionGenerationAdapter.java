package com.tmk.api.adapter.out.ai;

import com.tmk.core.port.out.ai.PrivateQuestionGenerationPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.vo.PrivateQuestionDraft;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
public class StubPrivateQuestionGenerationAdapter implements PrivateQuestionGenerationPort {

    @Override
    public List<PrivateQuestionDraft> generateQuestions(String languageCode, String documentTitle, List<String> chunkContents) {
        String baseContent = chunkContents.isEmpty() ? documentTitle : chunkContents.getFirst();
        return List.of(
                new PrivateQuestionDraft(
                        baseContent.length() > 80 ? baseContent.substring(0, 80) : baseContent,
                        QuestionType.SHORT_ANSWER,
                        Difficulty.NORMAL,
                        documentTitle,
                        "임시 생성 문제입니다.",
                        List.of()
                )
        );
    }
}
