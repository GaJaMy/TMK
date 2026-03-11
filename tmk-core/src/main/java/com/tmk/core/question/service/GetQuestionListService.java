package com.tmk.core.question.service;

import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetQuestionListService {

    private final QuestionRepository questionRepository;

    public List<Question> getList(QuestionType type, Difficulty difficulty, int page, int size) {
        // TODO
        return List.of();
    }
}
