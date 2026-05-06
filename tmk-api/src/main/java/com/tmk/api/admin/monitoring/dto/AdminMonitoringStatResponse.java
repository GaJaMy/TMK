package com.tmk.api.admin.monitoring.dto;

import com.tmk.api.admin.monitoring.result.AdminMonitoringSeriesResult;
import com.tmk.api.admin.monitoring.result.AdminMonitoringStatResult;
import java.util.List;

public record AdminMonitoringStatResponse(
        Summary summary,
        List<SeriesItem> series
) {
    public static AdminMonitoringStatResponse from(AdminMonitoringStatResult result) {
        List<AdminMonitoringSeriesResult> seriesResults = result.series();
        List<SeriesItem> seriesItems = seriesResults.stream()
                .map(SeriesItem::from)
                .toList();
        return new AdminMonitoringStatResponse(
                new Summary(result.totalCount()),
                seriesItems
        );
    }

    public record Summary(
            long totalCount
    ) {
    }

    public record SeriesItem(
            String label,
            long count
    ) {
        public static SeriesItem from(AdminMonitoringSeriesResult result) {
            return new SeriesItem(result.label(), result.count());
        }
    }
}
