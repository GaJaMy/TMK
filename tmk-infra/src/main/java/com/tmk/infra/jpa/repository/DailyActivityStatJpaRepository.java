package com.tmk.infra.jpa.repository;

import com.tmk.core.monitoring.entity.DailyActivityStat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyActivityStatJpaRepository extends JpaRepository<DailyActivityStat, Long> {

    Optional<DailyActivityStat> findByStatDate(LocalDate statDate);

    List<DailyActivityStat> findByStatDateBetweenOrderByStatDateAsc(LocalDate from, LocalDate to);
}
