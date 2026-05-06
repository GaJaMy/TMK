package com.tmk.api.admin.monitoring.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record AdminMonitoringStatRequest(
        @NotNull(message = "periodType은 필수입니다.")
        MonitoringPeriodType periodType,

        @NotNull(message = "from은 필수입니다.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @NotNull(message = "to는 필수입니다.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) {
}
