package com.tmk.batch.job;

import com.tmk.core.emailverification.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Configuration
@RequiredArgsConstructor
public class ExpiredVerificationCleanJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiredVerificationCleanJob.class);

    private final EmailVerificationRepository emailVerificationRepository;

    @Bean
    public Job expiredVerificationCleanJob(JobRepository jobRepository, Step expiredVerificationCleanStep) {
        return new JobBuilder("expiredVerificationCleanJob", jobRepository)
                .start(expiredVerificationCleanStep)
                .build();
    }

    @Bean
    public Step expiredVerificationCleanStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("expiredVerificationCleanStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    emailVerificationRepository.deleteByExpiredAtBefore(OffsetDateTime.now());
                    log.info("ExpiredVerificationCleanJob completed: deleted expired email verification codes");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
