package com.tmk.api.user.exam.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.user.exam.dto.ExamCreateResponse;
import com.tmk.api.user.exam.dto.ExamDetailResponse;
import com.tmk.api.user.exam.dto.ExamHistorySummaryResponse;
import com.tmk.api.user.exam.dto.ExamResultDetailResponse;
import com.tmk.api.user.exam.dto.ExamSummaryResponse;
import com.tmk.api.user.exam.dto.ExamStartResponse;
import com.tmk.api.user.exam.request.ExamAnswerSaveRequest;
import com.tmk.api.user.exam.request.ExamCreateRequest;
import com.tmk.api.user.exam.usecase.ExamUseCase;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController implements ExamControllerDocs {

    private final ExamUseCase examUseCase;

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<List<ExamSummaryResponse>>> getExams(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        return ApiResponse.ok(examUseCase.getExams(principal.getPrincipalId()));
    }

    @GetMapping("/history")
    @Override
    public ResponseEntity<ApiResponse<List<ExamHistorySummaryResponse>>> getExamHistory(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        return ApiResponse.ok(examUseCase.getExamHistory(principal.getPrincipalId()));
    }

    @GetMapping("/{examId}")
    @Override
    public ResponseEntity<ApiResponse<ExamDetailResponse>> getExam(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long examId
    ) {
        return ApiResponse.ok(examUseCase.getExam(principal.getPrincipalId(), examId));
    }

    @GetMapping("/{examId}/result")
    @Override
    public ResponseEntity<ApiResponse<ExamResultDetailResponse>> getExamResult(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long examId
    ) {
        return ApiResponse.ok(examUseCase.getExamResult(principal.getPrincipalId(), examId));
    }

    @PutMapping("/{examId}/answers")
    @Override
    public ResponseEntity<ApiResponse<Void>> saveAnswers(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long examId,
            @Valid @RequestBody List<@Valid ExamAnswerSaveRequest> requests
    ) {
        examUseCase.saveAnswers(principal.getPrincipalId(), examId, requests);
        return ApiResponse.ok();
    }

    @PostMapping("/{examId}/submit")
    @Override
    public ResponseEntity<ApiResponse<Void>> submitExam(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long examId
    ) {
        examUseCase.submitExam(principal.getPrincipalId(), examId);
        return ApiResponse.ok();
    }

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<ExamCreateResponse>> createExam(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ExamCreateRequest request
    ) {
        return ApiResponse.ok(examUseCase.createExam(principal.getPrincipalId(), request));
    }

    @PostMapping("/{examId}/start")
    @Override
    public ResponseEntity<ApiResponse<ExamStartResponse>> startExam(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long examId
    ) {
        return ApiResponse.ok(examUseCase.startExam(principal.getPrincipalId(), examId));
    }
}
