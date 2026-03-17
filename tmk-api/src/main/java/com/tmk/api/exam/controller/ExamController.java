package com.tmk.api.exam.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.exam.dto.AnswerCommand;
import com.tmk.api.exam.dto.ExamDetailResult;
import com.tmk.api.exam.dto.ExamResult;
import com.tmk.api.exam.dto.ExamResultData;
import com.tmk.api.exam.dto.HistoryDetailResult;
import com.tmk.api.exam.dto.HistoryListResult;
import com.tmk.api.exam.dto.SubmitResult;
import com.tmk.api.exam.usecase.ExamUseCase;
import com.tmk.api.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController implements ExamControllerDocs {

    private final ExamUseCase examUseCase;

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<ExamResult>> createExam(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ExamResult result = examUseCase.create(userDetails.getUserId());
        return ApiResponse.ok(result);
    }

    @GetMapping("/{examId}")
    @Override
    public ResponseEntity<ApiResponse<ExamDetailResult>> getExam(
            @PathVariable Long examId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ExamDetailResult result = examUseCase.getExam(examId, userDetails.getUserId());
        return ApiResponse.ok(result);
    }

    @PutMapping("/{examId}/answers")
    @Override
    public ResponseEntity<ApiResponse<Void>> saveAnswers(
            @PathVariable Long examId,
            @RequestBody List<AnswerCommand> answers,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        examUseCase.saveAnswers(examId, userDetails.getUserId(), answers);
        return ApiResponse.ok();
    }

    @PostMapping("/{examId}/submit")
    @Override
    public ResponseEntity<ApiResponse<SubmitResult>> submitExam(
            @PathVariable Long examId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SubmitResult result = examUseCase.submit(examId, userDetails.getUserId());
        return ApiResponse.ok(result);
    }

    @GetMapping("/{examId}/result")
    @Override
    public ResponseEntity<ApiResponse<ExamResultData>> getResult(
            @PathVariable Long examId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ExamResultData result = examUseCase.getResult(examId, userDetails.getUserId());
        return ApiResponse.ok(result);
    }

    @GetMapping("/history")
    @Override
    public ResponseEntity<ApiResponse<HistoryListResult>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        HistoryListResult result = examUseCase.getHistory(userDetails.getUserId(), page, size);
        return ApiResponse.ok(result);
    }

    @GetMapping("/history/{examId}")
    @Override
    public ResponseEntity<ApiResponse<HistoryDetailResult>> getHistoryDetail(
            @PathVariable Long examId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        HistoryDetailResult result = examUseCase.getHistoryDetail(examId, userDetails.getUserId());
        return ApiResponse.ok(result);
    }
}
