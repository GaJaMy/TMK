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
        List<Question> all;
        if (type != null && difficulty != null) {
            all = questionPort.findByTypeAndDifficulty(type, difficulty);
        } else {
            all = questionPort.findAll();
        }

        int start = page * size;
        if (start >= all.size()) return List.of();
        int end = Math.min(start + size, all.size());
        return all.subList(start, end);
    }

    public long count(QuestionType type, Difficulty difficulty) {
        if (type != null && difficulty != null) {
            return questionPort.findByTypeAndDifficulty(type, difficulty).size();
        }
        return questionPort.findAll().size();
    }
}
