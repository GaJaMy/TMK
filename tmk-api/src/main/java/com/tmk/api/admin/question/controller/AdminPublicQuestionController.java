package com.tmk.api.admin.question.controller;

import com.tmk.api.admin.question.dto.AdminPublicQuestionDetailResponse;
import com.tmk.api.admin.question.dto.AdminPublicQuestionSummaryResponse;
import com.tmk.api.admin.question.request.AdminPublicQuestionBulkDeleteRequest;
import com.tmk.api.admin.question.request.AdminPublicQuestionBulkStatusChangeRequest;
import com.tmk.api.admin.question.request.AdminPublicQuestionCreateRequest;
import com.tmk.api.admin.question.request.AdminPublicQuestionStatusChangeRequest;
import com.tmk.api.admin.question.usecase.AdminPublicQuestionUseCase;
import com.tmk.api.common.ApiResponse;
import com.tmk.api.common.ApiVersion;
import com.tmk.api.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPublicQuestionController implements AdminPublicQuestionControllerDocs {

    private final AdminPublicQuestionUseCase adminPublicQuestionUseCase;

    @GetMapping(ApiVersion.V1 + "/questions")
    @Override
    public ResponseEntity<ApiResponse<List<AdminPublicQuestionSummaryResponse>>> getPublicQuestions(
            @RequestParam(value = "topicId", required = false) Long topicId,
            @RequestParam(value = "difficulty", required = false) Difficulty difficulty,
            @RequestParam(value = "type", required = false) QuestionType type,
            @RequestParam(value = "active", required = false) Boolean active
    ) {
        return ApiResponse.ok(adminPublicQuestionUseCase.getPublicQuestions(topicId, difficulty, type, active));
    }

    @GetMapping(ApiVersion.V1 + "/questions/{questionId}")
    @Override
    public ResponseEntity<ApiResponse<AdminPublicQuestionDetailResponse>> getPublicQuestion(
            @PathVariable("questionId") Long questionId
    ) {
        return ApiResponse.ok(adminPublicQuestionUseCase.getPublicQuestion(questionId));
    }

    @PostMapping(ApiVersion.V1 + "/questions")
    @Override
    public ResponseEntity<ApiResponse<AdminPublicQuestionDetailResponse>> createPublicQuestion(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AdminPublicQuestionCreateRequest request
    ) {
        return ApiResponse.ok(adminPublicQuestionUseCase.createPublicQuestion(principal.getPrincipalId(), request));
    }

    @PatchMapping(ApiVersion.V1 + "/questions/{questionId}/status")
    @Override
    public ResponseEntity<ApiResponse<AdminPublicQuestionDetailResponse>> changePublicQuestionStatus(
            @PathVariable("questionId") Long questionId,
            @Valid @RequestBody AdminPublicQuestionStatusChangeRequest request
    ) {
        return ApiResponse.ok(adminPublicQuestionUseCase.changePublicQuestionStatus(questionId, request));
    }

    @PatchMapping(ApiVersion.V1 + "/questions/status")
    @Override
    public ResponseEntity<ApiResponse<Void>> changePublicQuestionStatuses(
            @Valid @RequestBody AdminPublicQuestionBulkStatusChangeRequest request
    ) {
        adminPublicQuestionUseCase.changePublicQuestionStatuses(request);
        return ApiResponse.noContent();
    }

    @DeleteMapping(ApiVersion.V1 + "/questions/{questionId}")
    @Override
    public ResponseEntity<ApiResponse<Void>> deletePublicQuestion(
            @PathVariable("questionId") Long questionId
    ) {
        adminPublicQuestionUseCase.deletePublicQuestion(questionId);
        return ApiResponse.noContent();
    }

    @DeleteMapping(ApiVersion.V1 + "/questions")
    @Override
    public ResponseEntity<ApiResponse<Void>> deletePublicQuestions(
            @Valid @RequestBody AdminPublicQuestionBulkDeleteRequest request
    ) {
        adminPublicQuestionUseCase.deletePublicQuestions(request);
        return ApiResponse.noContent();
    }
}
