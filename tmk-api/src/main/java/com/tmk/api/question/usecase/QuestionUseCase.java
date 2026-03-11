package com.tmk.api.question.usecase;

import com.tmk.api.question.dto.QuestionDetailResult;
import com.tmk.api.question.dto.QuestionListResult;
import com.tmk.core.question.service.GetQuestionDetailService;
import com.tmk.core.question.service.GetQuestionListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionUseCase {

    private final GetQuestionListService getQuestionListService;
    private final GetQuestionDetailService getQuestionDetailService;

    public QuestionListResult getList(String type, String difficulty, int page, int size) {
        // TODO: convert String type/difficulty to enum, call service, convert result
        return null;
    }

    public QuestionDetailResult getDetail(Long questionId) {
        getQuestionDetailService.getDetail(questionId);
        // TODO: convert Question to QuestionDetailResult
        return null;
    }
}
