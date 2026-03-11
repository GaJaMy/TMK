package com.tmk.core.question.service;

import com.tmk.core.question.entity.Question;
import com.tmk.core.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetQuestionDetailService {

    private final QuestionRepository questionRepository;

    public Question getDetail(Long questionId) {
        // TODO
        return null;
    }
}
