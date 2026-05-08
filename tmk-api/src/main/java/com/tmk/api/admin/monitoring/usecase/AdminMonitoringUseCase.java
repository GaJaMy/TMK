package com.tmk.api.admin.monitoring.usecase;

import com.tmk.api.admin.monitoring.dto.AdminMonitoringStatResponse;
import com.tmk.api.admin.monitoring.request.AdminMonitoringStatRequest;
import com.tmk.api.admin.monitoring.result.AdminMonitoringStatResult;
import com.tmk.api.admin.monitoring.service.AdminMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMonitoringUseCase {

    private final AdminMonitoringService adminMonitoringService;

    public AdminMonitoringStatResponse getUserPageAccessAttempts(AdminMonitoringStatRequest request) {
        AdminMonitoringStatResult result = adminMonitoringService.getUserPageAccessAttempts(
                request.periodType(),
                request.from(),
                request.to()
        );
        return AdminMonitoringStatResponse.from(result);
    }

    public AdminMonitoringStatResponse getExamRuns(AdminMonitoringStatRequest request) {
        AdminMonitoringStatResult result = adminMonitoringService.getExamRuns(
                request.periodType(),
                request.from(),
                request.to()
        );
        return AdminMonitoringStatResponse.from(result);
    }

    public AdminMonitoringStatResponse getDocumentRegistrations(AdminMonitoringStatRequest request) {
        AdminMonitoringStatResult result = adminMonitoringService.getDocumentRegistrations(
                request.periodType(),
                request.from(),
                request.to()
        );
        return AdminMonitoringStatResponse.from(result);
    }

    public AdminMonitoringStatResponse getQuestionGenerations(AdminMonitoringStatRequest request) {
        AdminMonitoringStatResult result = adminMonitoringService.getQuestionGenerations(
                request.periodType(),
                request.from(),
                request.to()
        );
        return AdminMonitoringStatResponse.from(result);
    }
}
