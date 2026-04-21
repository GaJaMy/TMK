package com.tmk.api.question.usecase;

import com.tmk.api.question.dto.OptionResult;
import com.tmk.api.question.dto.QuestionDetailResult;
import com.tmk.api.question.dto.QuestionListResult;
import com.tmk.api.question.dto.QuestionSummary;
import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.service.GetQuestionDetailService;
import com.tmk.core.question.service.GetQuestionListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionUseCase {

    private final GetQuestionListService getQuestionListService;
    private final GetQuestionDetailService getQuestionDetailService;

    public QuestionListResult getList(String type, String difficulty, String topic, String scope, Long userId, int page, int size) {
        QuestionType questionType = (type != null && !type.isBlank())
                ? QuestionType.valueOf(type.toUpperCase()) : null;
        Difficulty diff = (difficulty != null && !difficulty.isBlank())
                ? Difficulty.valueOf(difficulty.toUpperCase()) : null;
        Topic selectedTopic = (topic != null && !topic.isBlank())
                ? Topic.valueOf(topic.toUpperCase()) : null;
        ContentScope selectedScope = (scope != null && !scope.isBlank())
                ? ContentScope.valueOf(scope.toUpperCase()) : ContentScope.PUBLIC;
        Long ownerUserId = selectedScope == ContentScope.PRIVATE ? userId : null;

        List<Question> questions = getQuestionListService.getList(questionType, diff, selectedTopic, selectedScope, ownerUserId, page, size);
        long total = getQuestionListService.count(questionType, diff, selectedTopic, selectedScope, ownerUserId);
        int totalPages = (size > 0) ? (int) Math.ceil((double) total / size) : 0;

        List<QuestionSummary> summaries = questions.stream()
                .map(q -> new QuestionSummary(
                        q.getId(),
                        q.getContent(),
                        q.getType().name(),
                        q.getDifficulty().name(),
                        q.getTopic().name(),
                        q.getScope().name(),
                        q.getSourceType().name()
                ))
                .toList();
        return new QuestionListResult(summaries, page, size, total, totalPages);
    }

    public QuestionDetailResult getDetail(Long questionId, Long userId) {
        Question question = getQuestionDetailService.getAccessibleDetail(questionId, userId);
        List<OptionResult> options = question.getOptions().stream()
                .map(o -> new OptionResult(o.getOptionNumber().intValue(), o.getContent()))
                .toList();
        return new QuestionDetailResult(
                question.getId(),
                question.getContent(),
                question.getType().name(),
                question.getDifficulty().name(),
                question.getTopic().name(),
                question.getScope().name(),
                question.getSourceType().name(),
                options,
                question.getAnswer(),
                question.getExplanation()
        );
    }
}
