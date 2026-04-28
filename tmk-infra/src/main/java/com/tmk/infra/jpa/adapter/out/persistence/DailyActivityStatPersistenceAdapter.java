package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.monitoring.entity.DailyActivityStat;
import com.tmk.core.port.out.persistence.DailyActivityStatPort;
import com.tmk.infra.jpa.repository.DailyActivityStatJpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyActivityStatPersistenceAdapter implements DailyActivityStatPort {

    private final DailyActivityStatJpaRepository dailyActivityStatJpaRepository;

    @Override
    public DailyActivityStat save(DailyActivityStat dailyActivityStat) {
        return dailyActivityStatJpaRepository.save(dailyActivityStat);
    }

    @Override
    public Optional<DailyActivityStat> findByStatDate(LocalDate statDate) {
        return dailyActivityStatJpaRepository.findByStatDate(statDate);
    }

    @Override
    public List<DailyActivityStat> findByStatDateBetweenOrderByStatDateAsc(LocalDate from, LocalDate to) {
        return dailyActivityStatJpaRepository.findByStatDateBetweenOrderByStatDateAsc(from, to);
    }
}
