package com.tmk.api.user.exam.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.api.user.exam.dto.ExamCreateResponse;
import com.tmk.api.user.exam.dto.ExamSummaryResponse;
import com.tmk.api.user.exam.dto.ExamStartResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exams")
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
