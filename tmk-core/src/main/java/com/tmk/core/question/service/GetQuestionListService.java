package com.tmk.core.question.service;

import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.port.out.persistence.QuestionPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetQuestionListService {

    private final QuestionPort questionPort;

    public List<Question> getList(QuestionType type, Difficulty difficulty, Topic topic, ContentScope scope, Long ownerUserId, int page, int size) {
        int offset = page * size;
        return questionPort.findByFilters(type, difficulty, topic, scope, ownerUserId, offset, size);
    }

    public long count(QuestionType type, Difficulty difficulty, Topic topic, ContentScope scope, Long ownerUserId) {
        return questionPort.countByFilters(type, difficulty, topic, scope, ownerUserId);
    }
}
