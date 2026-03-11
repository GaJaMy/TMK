package com.tmk.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ExpiredVerificationCleanJob {

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
                    // TODO: 만료된 이메일 인증 코드 정리
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
