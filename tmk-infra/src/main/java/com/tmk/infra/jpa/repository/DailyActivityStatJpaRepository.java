package com.tmk.infra.jpa.repository;

import com.tmk.core.monitoring.entity.DailyActivityStat;
import com.tmk.core.port.out.persistence.DailyActivityStatPort.DailyActivityStatSummary;
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

    @Query("""
            select new com.tmk.core.port.out.persistence.DailyActivityStatPort$DailyActivityStatSummary(
                coalesce(sum(stat.userPageAccessAttemptCount), 0),
                coalesce(sum(stat.examRunCount), 0),
                coalesce(sum(stat.documentRegistrationCount), 0),
                coalesce(sum(stat.generatedPrivateQuestionCount), 0)
            )
            from DailyActivityStat stat
            """)
    DailyActivityStatSummary findSummary();

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
                0,
                :value,
                0,
                0,
                :now,
                :now
            )
            on conflict (stat_date) do update
            set exam_run_count = daily_activity_stat.exam_run_count + excluded.exam_run_count,
                updated_at = excluded.updated_at
            """, nativeQuery = true)
    void increaseExamRunCount(
            @Param("statDate") LocalDate statDate,
            @Param("value") int value,
            @Param("now") OffsetDateTime now
    );

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
                0,
                0,
                :value,
                0,
                :now,
                :now
            )
            on conflict (stat_date) do update
            set document_registration_count = daily_activity_stat.document_registration_count + excluded.document_registration_count,
                updated_at = excluded.updated_at
            """, nativeQuery = true)
    void increaseDocumentRegistrationCount(
            @Param("statDate") LocalDate statDate,
            @Param("value") int value,
            @Param("now") OffsetDateTime now
    );

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
                0,
                0,
                0,
                :value,
                :now,
                :now
            )
            on conflict (stat_date) do update
            set generated_private_question_count =
                    daily_activity_stat.generated_private_question_count + excluded.generated_private_question_count,
                updated_at = excluded.updated_at
            """, nativeQuery = true)
    void increaseGeneratedPrivateQuestionCount(
            @Param("statDate") LocalDate statDate,
            @Param("value") int value,
            @Param("now") OffsetDateTime now
    );
}
