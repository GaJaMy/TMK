package com.tmk.core.port.out.persistence;

import com.tmk.core.monitoring.entity.DailyActivityStat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyActivityStatPort {

    DailyActivityStat save(DailyActivityStat dailyActivityStat);

    Optional<DailyActivityStat> findByStatDate(LocalDate statDate);

    List<DailyActivityStat> findByStatDateBetweenOrderByStatDateAsc(LocalDate from, LocalDate to);
}
