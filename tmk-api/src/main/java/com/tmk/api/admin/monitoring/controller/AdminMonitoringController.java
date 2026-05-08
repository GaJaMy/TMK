package com.tmk.api.admin.monitoring.controller;

import com.tmk.api.admin.monitoring.dto.AdminMonitoringStatResponse;
import com.tmk.api.admin.monitoring.request.AdminMonitoringStatRequest;
import com.tmk.api.admin.monitoring.usecase.AdminMonitoringUseCase;
import com.tmk.api.common.ApiResponse;
import com.tmk.api.common.ApiVersion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminMonitoringController implements AdminMonitoringControllerDocs {

    private final AdminMonitoringUseCase adminMonitoringUseCase;

    @GetMapping(ApiVersion.V1 + "/monitoring/access-attempts")
    @Override
    public ResponseEntity<ApiResponse<AdminMonitoringStatResponse>> getUserPageAccessAttempts(
            @Valid @ModelAttribute AdminMonitoringStatRequest request
    ) {
        return ApiResponse.ok(adminMonitoringUseCase.getUserPageAccessAttempts(request));
    }

    @GetMapping(ApiVersion.V1 + "/monitoring/exam-runs")
    @Override
    public ResponseEntity<ApiResponse<AdminMonitoringStatResponse>> getExamRuns(
            @Valid @ModelAttribute AdminMonitoringStatRequest request
    ) {
        return ApiResponse.ok(adminMonitoringUseCase.getExamRuns(request));
    }

    @GetMapping(ApiVersion.V1 + "/monitoring/document-registrations")
    @Override
    public ResponseEntity<ApiResponse<AdminMonitoringStatResponse>> getDocumentRegistrations(
            @Valid @ModelAttribute AdminMonitoringStatRequest request
    ) {
        return ApiResponse.ok(adminMonitoringUseCase.getDocumentRegistrations(request));
    }

    @GetMapping(ApiVersion.V1 + "/monitoring/question-generations")
    @Override
    public ResponseEntity<ApiResponse<AdminMonitoringStatResponse>> getQuestionGenerations(
            @Valid @ModelAttribute AdminMonitoringStatRequest request
    ) {
        return ApiResponse.ok(adminMonitoringUseCase.getQuestionGenerations(request));
    }
}
