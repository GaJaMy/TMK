package com.tmk.core.question.service;

import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.port.out.persistence.QuestionPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionOption;
import com.tmk.core.question.entity.QuestionSourceType;
import com.tmk.core.question.entity.QuestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegisterPublicQuestionService {

    private final QuestionPort questionPort;

    @Transactional
    public Question register(Topic topic, String content, QuestionType type,
                             Difficulty difficulty, String answer,
                             String explanation, List<String> options) {
        OffsetDateTime now = OffsetDateTime.now();
        Question question = Question.builder()
                .documentId(null)
                .ownerUserId(null)
                .scope(ContentScope.PUBLIC)
                .sourceType(QuestionSourceType.ADMIN_MANUAL)
                .content(content)
                .type(type)
                .difficulty(difficulty)
                .topic(topic)
                .answer(answer)
                .explanation(explanation)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Question saved = questionPort.save(question);
        if (type == QuestionType.MULTIPLE_CHOICE && options != null) {
            for (int i = 0; i < options.size(); i++) {
                saved.getOptions().add(QuestionOption.builder()
                        .question(saved)
                        .optionNumber((short) (i + 1))
                        .content(options.get(i))
                        .build());
            }
            saved = questionPort.save(saved);
        }
        return saved;
    }
}
