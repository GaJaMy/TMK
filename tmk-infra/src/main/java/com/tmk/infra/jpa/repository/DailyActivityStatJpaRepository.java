package com.tmk.infra.jpa.repository;

import com.tmk.core.monitoring.entity.DailyActivityStat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface DailyActivityStatJpaRepository extends JpaRepository<DailyActivityStat, Long> {

    Optional<DailyActivityStat> findByStatDate(LocalDate statDate);

    List<DailyActivityStat> findByStatDateBetweenOrderByStatDateAsc(LocalDate from, LocalDate to);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            insert into daily_activity_stat (
                stat_date,
                user_page_access_attempt_count,
                exam_run_count,
                document_registration_count,
                generated_private_question_count,
                created_at,
                updated_at
            )
            values (
                :statDate,
                :value,
                0,
                0,
                0,
                :now,
                :now
            )
            on conflict (stat_date) do update
            set user_page_access_attempt_count = daily_activity_stat.user_page_access_attempt_count + excluded.user_page_access_attempt_count,
                updated_at = excluded.updated_at
            """, nativeQuery = true)
    void increaseUserPageAccessAttemptCount(
            @Param("statDate") LocalDate statDate,
            @Param("value") int value,
            @Param("now") OffsetDateTime now
    );
}
