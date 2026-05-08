package com.tmk.api.admin.monitoring.service;

import com.tmk.api.admin.monitoring.request.MonitoringPeriodType;
import com.tmk.api.admin.monitoring.result.AdminMonitoringSeriesResult;
import com.tmk.api.admin.monitoring.result.AdminMonitoringStatResult;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.monitoring.entity.DailyActivityStat;
import com.tmk.core.port.out.persistence.DailyActivityStatPort;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMonitoringService {

    private final DailyActivityStatPort dailyActivityStatPort;

    @Transactional(readOnly = true)
    public AdminMonitoringStatResult getUserPageAccessAttempts(
            MonitoringPeriodType periodType,
            LocalDate from,
            LocalDate to
    ) {
        validatePeriod(periodType, from, to);

        List<DailyActivityStat> dailyActivityStats =
                dailyActivityStatPort.findByStatDateBetweenOrderByStatDateAsc(from, to);
        return createStatResult(
                periodType,
                from,
                to,
                dailyActivityStats,
                DailyActivityStat::getUserPageAccessAttemptCount
        );
    }

    @Transactional(readOnly = true)
    public AdminMonitoringStatResult getExamRuns(
            MonitoringPeriodType periodType,
            LocalDate from,
            LocalDate to
    ) {
        validatePeriod(periodType, from, to);

        List<DailyActivityStat> dailyActivityStats =
                dailyActivityStatPort.findByStatDateBetweenOrderByStatDateAsc(from, to);
        return createStatResult(
                periodType,
                from,
                to,
                dailyActivityStats,
                DailyActivityStat::getExamRunCount
        );
    }

    @Transactional(readOnly = true)
    public AdminMonitoringStatResult getDocumentRegistrations(
            MonitoringPeriodType periodType,
            LocalDate from,
            LocalDate to
    ) {
        validatePeriod(periodType, from, to);

        List<DailyActivityStat> dailyActivityStats =
                dailyActivityStatPort.findByStatDateBetweenOrderByStatDateAsc(from, to);
        return createStatResult(
                periodType,
                from,
                to,
                dailyActivityStats,
                DailyActivityStat::getDocumentRegistrationCount
        );
    }

    @Transactional(readOnly = true)
    public AdminMonitoringStatResult getQuestionGenerations(
            MonitoringPeriodType periodType,
            LocalDate from,
            LocalDate to
    ) {
        validatePeriod(periodType, from, to);

        List<DailyActivityStat> dailyActivityStats =
                dailyActivityStatPort.findByStatDateBetweenOrderByStatDateAsc(from, to);
        return createStatResult(
                periodType,
                from,
                to,
                dailyActivityStats,
                DailyActivityStat::getGeneratedPrivateQuestionCount
        );
    }

    @Transactional
    public void increaseUserPageAccessAttemptCount(LocalDate statDate, int value) {
        dailyActivityStatPort.increaseUserPageAccessAttemptCount(statDate, value, OffsetDateTime.now());
    }

    private void validatePeriod(MonitoringPeriodType periodType, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException(ErrorCode.INVALID_MONITORING_PERIOD);
        }

        if (periodType == MonitoringPeriodType.WEEKLY) {
            if (from.getDayOfWeek() != DayOfWeek.MONDAY || to.getDayOfWeek() != DayOfWeek.SUNDAY) {
                throw new BusinessException(ErrorCode.INVALID_MONITORING_PERIOD);
            }
        }

        if (periodType == MonitoringPeriodType.MONTHLY) {
            if (from.getDayOfMonth() != 1 || to.getDayOfMonth() != to.lengthOfMonth()) {
                throw new BusinessException(ErrorCode.INVALID_MONITORING_PERIOD);
            }
        }
    }

    private AdminMonitoringStatResult createStatResult(
            MonitoringPeriodType periodType,
            LocalDate from,
            LocalDate to,
            List<DailyActivityStat> dailyActivityStats,
            ToIntFunction<DailyActivityStat> countExtractor
    ) {
        long totalCount = dailyActivityStats.stream()
                .mapToLong(dailyActivityStat -> countExtractor.applyAsInt(dailyActivityStat))
                .sum();
        Map<LocalDate, Integer> countByDate = createCountByDate(dailyActivityStats, countExtractor);
        List<AdminMonitoringSeriesResult> series = switch (periodType) {
            case DAILY -> createDailySeries(from, to, countByDate);
            case WEEKLY -> createWeeklySeries(from, to, countByDate);
            case MONTHLY -> createMonthlySeries(from, to, countByDate);
        };
        return new AdminMonitoringStatResult(totalCount, series);
    }

    private Map<LocalDate, Integer> createCountByDate(
            List<DailyActivityStat> dailyActivityStats,
            ToIntFunction<DailyActivityStat> countExtractor
    ) {
        Map<LocalDate, Integer> countByDate = new HashMap<>();
        for (DailyActivityStat dailyActivityStat : dailyActivityStats) {
            countByDate.put(dailyActivityStat.getStatDate(), countExtractor.applyAsInt(dailyActivityStat));
        }
        return countByDate;
    }

    private List<AdminMonitoringSeriesResult> createDailySeries(
            LocalDate from,
            LocalDate to,
            Map<LocalDate, Integer> countByDate
    ) {
        List<AdminMonitoringSeriesResult> series = new ArrayList<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            series.add(new AdminMonitoringSeriesResult(
                    current.toString(),
                    countByDate.getOrDefault(current, 0)
            ));
            current = current.plusDays(1);
        }
        return series;
    }

    private List<AdminMonitoringSeriesResult> createWeeklySeries(
            LocalDate from,
            LocalDate to,
            Map<LocalDate, Integer> countByDate
    ) {
        List<AdminMonitoringSeriesResult> series = new ArrayList<>();
        LocalDate currentStart = from;
        while (!currentStart.isAfter(to)) {
            LocalDate currentEnd = currentStart.plusDays(6);
            long count = sumCounts(currentStart, currentEnd, countByDate);
            series.add(new AdminMonitoringSeriesResult(
                    currentStart + " ~ " + currentEnd,
                    count
            ));
            currentStart = currentEnd.plusDays(1);
        }
        return series;
    }

    private List<AdminMonitoringSeriesResult> createMonthlySeries(
            LocalDate from,
            LocalDate to,
            Map<LocalDate, Integer> countByDate
    ) {
        List<AdminMonitoringSeriesResult> series = new ArrayList<>();
        YearMonth current = YearMonth.from(from);
        YearMonth last = YearMonth.from(to);
        while (!current.isAfter(last)) {
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();
            long count = sumCounts(monthStart, monthEnd, countByDate);
            series.add(new AdminMonitoringSeriesResult(current.toString(), count));
            current = current.plusMonths(1);
        }
        return series;
    }

    private long sumCounts(LocalDate from, LocalDate to, Map<LocalDate, Integer> countByDate) {
        long count = 0;
        LocalDate current = from;
        while (!current.isAfter(to)) {
            count += countByDate.getOrDefault(current, 0);
            current = current.plusDays(1);
        }
        return count;
    }
}
