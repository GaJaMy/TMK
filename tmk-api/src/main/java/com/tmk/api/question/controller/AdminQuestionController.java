package com.tmk.api.question.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.question.dto.OptionResult;
import com.tmk.api.question.dto.QuestionDetailResult;
import com.tmk.api.question.request.AdminCreateQuestionRequest;
import com.tmk.core.common.Topic;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.service.RegisterPublicQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/v1/questions")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final RegisterPublicQuestionService registerPublicQuestionService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuestionDetailResult>> create(@RequestBody AdminCreateQuestionRequest request) {
        Question question = registerPublicQuestionService.register(
                Topic.valueOf(request.getTopic().toUpperCase()),
                request.getContent(),
                QuestionType.valueOf(request.getType().toUpperCase()),
                Difficulty.valueOf(request.getDifficulty().toUpperCase()),
                request.getAnswer(),
                request.getExplanation(),
                request.getOptions()
        );

        List<OptionResult> options = question.getOptions().stream()
                .map(option -> new OptionResult(option.getOptionNumber().intValue(), option.getContent()))
                .toList();

        return ApiResponse.ok(new QuestionDetailResult(
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
        ));
    }
}
