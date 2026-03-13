package com.tmk.api.question.usecase;

import com.tmk.api.question.dto.OptionResult;
import com.tmk.api.question.dto.QuestionDetailResult;
import com.tmk.api.question.dto.QuestionListResult;
import com.tmk.api.question.dto.QuestionSummary;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.service.GetQuestionDetailService;
import com.tmk.core.question.service.GetQuestionListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuestionUseCase {

    private final GetQuestionListService getQuestionListService;
    private final GetQuestionDetailService getQuestionDetailService;

    public QuestionListResult getList(String type, String difficulty, int page, int size) {
        QuestionType questionType = (type != null && !type.isBlank()) ? QuestionType.valueOf(type) : null;
        Difficulty diff = (difficulty != null && !difficulty.isBlank()) ? Difficulty.valueOf(difficulty) : null;

        List<Question> questions = getQuestionListService.getList(questionType, diff, page, size);
        long totalElements = getQuestionListService.count(questionType, diff);
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        List<QuestionSummary> content = questions.stream()
                .map(q -> new QuestionSummary(
                        q.getId(),
                        q.getContent(),
                        q.getType().name(),
                        q.getDifficulty().name()
                ))
                .collect(Collectors.toList());

        return new QuestionListResult(content, page, size, totalElements, totalPages);
    }

    public QuestionDetailResult getDetail(Long questionId) {
        Question question = getQuestionDetailService.getDetail(questionId);

        List<OptionResult> options = question.getOptions().stream()
                .map(opt -> new OptionResult(opt.getOptionNumber(), opt.getContent()))
                .collect(Collectors.toList());

        return new QuestionDetailResult(
                question.getId(),
                question.getContent(),
                question.getType().name(),
                question.getDifficulty().name(),
                options,
                question.getAnswer(),
                question.getExplanation()
        );
    }
}
