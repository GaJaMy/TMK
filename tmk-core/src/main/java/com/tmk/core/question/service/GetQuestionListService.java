package com.tmk.core.question.service;

import com.tmk.core.port.out.QuestionPort;
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

    public List<Question> getList(QuestionType type, Difficulty difficulty, int page, int size) {
        // TODO
        return null;
    }

    public long count(QuestionType type, Difficulty difficulty) {
        // TODO
        return 0;
    }
}
