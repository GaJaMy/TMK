package com.tmk.batch.job;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.service.SubmitExamService;
import com.tmk.core.port.out.ExamPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExamAutoSubmitJob {

    private final SubmitExamService submitExamService;
    private final ExamPort examPort;

    @Bean
    public Job examAutoSubmitJob(JobRepository jobRepository, Step examAutoSubmitStep) {
        return new JobBuilder("examAutoSubmitJob", jobRepository)
                .start(examAutoSubmitStep)
                .build();
    }

    @Bean
    public Step examAutoSubmitStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("examAutoSubmitStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    List<Exam> expired = examPort.findExpiredInProgressExams(now);

                    log.info("Auto-submitting {} expired exams", expired.size());
                    for (Exam exam : expired) {
                        try {
                            submitExamService.submit(exam.getId(), exam.getUserId());
                            log.info("Auto-submitted exam id={}", exam.getId());
                        } catch (Exception e) {
                            log.error("Failed to auto-submit exam id={}: {}", exam.getId(), e.getMessage());
                        }
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
