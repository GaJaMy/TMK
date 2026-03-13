package com.tmk.core.question.service;

import com.tmk.core.port.out.QuestionPort;
import com.tmk.core.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetQuestionDetailService {

    private final QuestionPort questionPort;

    public Question getDetail(Long questionId) {
        // TODO
        return null;
    }
}
