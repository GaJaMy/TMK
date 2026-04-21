package com.tmk.api.question.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.question.dto.QuestionDetailResult;
import com.tmk.api.question.dto.QuestionListResult;
import com.tmk.api.question.usecase.QuestionUseCase;
import com.tmk.api.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController implements QuestionControllerDocs {

    private final QuestionUseCase questionUseCase;

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<QuestionListResult>> getList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "PUBLIC") String scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        QuestionListResult result = questionUseCase.getList(type, difficulty, topic, scope, userDetails.getUserId(), page, size);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{questionId}")
    @Override
    public ResponseEntity<ApiResponse<QuestionDetailResult>> getDetail(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        QuestionDetailResult result = questionUseCase.getDetail(questionId, userDetails.getUserId());
        return ApiResponse.ok(result);
    }
}
