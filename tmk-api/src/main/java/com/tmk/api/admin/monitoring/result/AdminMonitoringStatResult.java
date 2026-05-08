package com.tmk.api.admin.monitoring.result;

import java.util.List;

public record AdminMonitoringStatResult(
        long totalCount,
        List<AdminMonitoringSeriesResult> series
) {
}
