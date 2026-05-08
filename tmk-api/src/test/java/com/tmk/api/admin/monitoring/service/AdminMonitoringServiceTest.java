package com.tmk.api.admin.monitoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.tmk.api.admin.monitoring.request.AdminMonitoringStatRequest;
import com.tmk.api.admin.monitoring.request.MonitoringPeriodType;
import com.tmk.api.admin.monitoring.result.AdminMonitoringSeriesResult;
import com.tmk.api.admin.monitoring.result.AdminMonitoringStatResult;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.monitoring.entity.DailyActivityStat;
import com.tmk.core.port.out.persistence.DailyActivityStatPort;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMonitoringServiceTest {

    @Mock
    private DailyActivityStatPort dailyActivityStatPort;

    @InjectMocks
    private AdminMonitoringService adminMonitoringService;

    @Test
    void getUserPageAccessAttemptsReturnsDailySeriesWithZeroFill() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 3);
        OffsetDateTime now = OffsetDateTime.parse("2026-05-01T00:00:00+09:00");
        List<DailyActivityStat> dailyActivityStats = List.of(
                DailyActivityStat.builder()
                        .id(1L)
                        .statDate(from)
                        .userPageAccessAttemptCount(5)
                        .examRunCount(0)
                        .documentRegistrationCount(0)
                        .generatedPrivateQuestionCount(0)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                DailyActivityStat.builder()
                        .id(2L)
                        .statDate(to)
                        .userPageAccessAttemptCount(3)
                        .examRunCount(0)
                        .documentRegistrationCount(0)
                        .generatedPrivateQuestionCount(0)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );
        AdminMonitoringStatRequest request = new AdminMonitoringStatRequest(MonitoringPeriodType.DAILY, from, to);

        given(dailyActivityStatPort.findByStatDateBetweenOrderByStatDateAsc(from, to)).willReturn(dailyActivityStats);

        AdminMonitoringStatResult result =
                adminMonitoringService.getUserPageAccessAttempts(request.periodType(), request.from(), request.to());

        assertThat(result.totalCount()).isEqualTo(8);
        assertThat(result.series()).containsExactly(
                new AdminMonitoringSeriesResult("2026-05-01", 5),
                new AdminMonitoringSeriesResult("2026-05-02", 0),
                new AdminMonitoringSeriesResult("2026-05-03", 3)
        );
    }

    @Test
    void getUserPageAccessAttemptsReturnsWeeklySeries() {
        LocalDate from = LocalDate.of(2026, 5, 4);
        LocalDate to = LocalDate.of(2026, 5, 17);
        OffsetDateTime now = OffsetDateTime.parse("2026-05-04T00:00:00+09:00");
        List<DailyActivityStat> dailyActivityStats = List.of(
                createDailyActivityStat(1L, LocalDate.of(2026, 5, 4), 2, now),
                createDailyActivityStat(2L, LocalDate.of(2026, 5, 6), 3, now),
                createDailyActivityStat(3L, LocalDate.of(2026, 5, 11), 7, now)
        );
        AdminMonitoringStatRequest request = new AdminMonitoringStatRequest(MonitoringPeriodType.WEEKLY, from, to);

        given(dailyActivityStatPort.findByStatDateBetweenOrderByStatDateAsc(from, to)).willReturn(dailyActivityStats);

        AdminMonitoringStatResult result =
                adminMonitoringService.getUserPageAccessAttempts(request.periodType(), request.from(), request.to());

        assertThat(result.totalCount()).isEqualTo(12);
        assertThat(result.series()).containsExactly(
                new AdminMonitoringSeriesResult("2026-05-04 ~ 2026-05-10", 5),
                new AdminMonitoringSeriesResult("2026-05-11 ~ 2026-05-17", 7)
        );
    }

    @Test
    void getExamRunsReturnsDailySeriesWithZeroFill() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 3);
        OffsetDateTime now = OffsetDateTime.parse("2026-05-01T00:00:00+09:00");
        List<DailyActivityStat> dailyActivityStats = List.of(
                DailyActivityStat.builder()
                        .id(1L)
                        .statDate(from)
                        .userPageAccessAttemptCount(0)
                        .examRunCount(4)
                        .documentRegistrationCount(0)
                        .generatedPrivateQuestionCount(0)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                DailyActivityStat.builder()
                        .id(2L)
                        .statDate(to)
                        .userPageAccessAttemptCount(0)
                        .examRunCount(2)
                        .documentRegistrationCount(0)
                        .generatedPrivateQuestionCount(0)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );
        AdminMonitoringStatRequest request = new AdminMonitoringStatRequest(MonitoringPeriodType.DAILY, from, to);

        given(dailyActivityStatPort.findByStatDateBetweenOrderByStatDateAsc(from, to)).willReturn(dailyActivityStats);

        AdminMonitoringStatResult result =
                adminMonitoringService.getExamRuns(request.periodType(), request.from(), request.to());

        assertThat(result.totalCount()).isEqualTo(6);
        assertThat(result.series()).containsExactly(
                new AdminMonitoringSeriesResult("2026-05-01", 4),
                new AdminMonitoringSeriesResult("2026-05-02", 0),
                new AdminMonitoringSeriesResult("2026-05-03", 2)
        );
    }

    @Test
    void getDocumentRegistrationsReturnsDailySeriesWithZeroFill() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 3);
        OffsetDateTime now = OffsetDateTime.parse("2026-05-01T00:00:00+09:00");
        List<DailyActivityStat> dailyActivityStats = List.of(
                DailyActivityStat.builder()
                        .id(1L)
                        .statDate(from)
                        .userPageAccessAttemptCount(0)
                        .examRunCount(0)
                        .documentRegistrationCount(3)
                        .generatedPrivateQuestionCount(0)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                DailyActivityStat.builder()
                        .id(2L)
                        .statDate(to)
                        .userPageAccessAttemptCount(0)
                        .examRunCount(0)
                        .documentRegistrationCount(1)
                        .generatedPrivateQuestionCount(0)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );
        AdminMonitoringStatRequest request = new AdminMonitoringStatRequest(MonitoringPeriodType.DAILY, from, to);

        given(dailyActivityStatPort.findByStatDateBetweenOrderByStatDateAsc(from, to)).willReturn(dailyActivityStats);

        AdminMonitoringStatResult result =
                adminMonitoringService.getDocumentRegistrations(request.periodType(), request.from(), request.to());

        assertThat(result.totalCount()).isEqualTo(4);
        assertThat(result.series()).containsExactly(
                new AdminMonitoringSeriesResult("2026-05-01", 3),
                new AdminMonitoringSeriesResult("2026-05-02", 0),
                new AdminMonitoringSeriesResult("2026-05-03", 1)
        );
    }

    @Test
    void getQuestionGenerationsReturnsDailySeriesWithZeroFill() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 3);
        OffsetDateTime now = OffsetDateTime.parse("2026-05-01T00:00:00+09:00");
        List<DailyActivityStat> dailyActivityStats = List.of(
                DailyActivityStat.builder()
                        .id(1L)
                        .statDate(from)
                        .userPageAccessAttemptCount(0)
                        .examRunCount(0)
                        .documentRegistrationCount(0)
                        .generatedPrivateQuestionCount(6)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                DailyActivityStat.builder()
                        .id(2L)
                        .statDate(to)
                        .userPageAccessAttemptCount(0)
                        .examRunCount(0)
                        .documentRegistrationCount(0)
                        .generatedPrivateQuestionCount(2)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );
        AdminMonitoringStatRequest request = new AdminMonitoringStatRequest(MonitoringPeriodType.DAILY, from, to);

        given(dailyActivityStatPort.findByStatDateBetweenOrderByStatDateAsc(from, to)).willReturn(dailyActivityStats);

        AdminMonitoringStatResult result =
                adminMonitoringService.getQuestionGenerations(request.periodType(), request.from(), request.to());

        assertThat(result.totalCount()).isEqualTo(8);
        assertThat(result.series()).containsExactly(
                new AdminMonitoringSeriesResult("2026-05-01", 6),
                new AdminMonitoringSeriesResult("2026-05-02", 0),
                new AdminMonitoringSeriesResult("2026-05-03", 2)
        );
    }

    @Test
    void getUserPageAccessAttemptsThrowsWhenMonthlyPeriodIsInvalid() {
        AdminMonitoringStatRequest request = new AdminMonitoringStatRequest(
                MonitoringPeriodType.MONTHLY,
                LocalDate.of(2026, 5, 2),
                LocalDate.of(2026, 5, 31)
        );

        assertThatThrownBy(() ->
                adminMonitoringService.getUserPageAccessAttempts(request.periodType(), request.from(), request.to()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_MONITORING_PERIOD.getMessage());
    }

    @Test
    void getUserPageAccessAttemptsThrowsWhenFromIsAfterTo() {
        AdminMonitoringStatRequest request = new AdminMonitoringStatRequest(
                MonitoringPeriodType.DAILY,
                LocalDate.of(2026, 5, 3),
                LocalDate.of(2026, 5, 1)
        );

        assertThatThrownBy(() ->
                adminMonitoringService.getUserPageAccessAttempts(request.periodType(), request.from(), request.to()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_MONITORING_PERIOD.getMessage());
    }

    private DailyActivityStat createDailyActivityStat(
            Long id,
            LocalDate statDate,
            int userPageAccessAttemptCount,
            OffsetDateTime now
    ) {
        return DailyActivityStat.builder()
                .id(id)
                .statDate(statDate)
                .userPageAccessAttemptCount(userPageAccessAttemptCount)
                .examRunCount(0)
                .documentRegistrationCount(0)
                .generatedPrivateQuestionCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
