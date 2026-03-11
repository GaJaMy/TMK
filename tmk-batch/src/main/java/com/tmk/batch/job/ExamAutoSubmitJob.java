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
public class ExamAutoSubmitJob {

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
                    // TODO: 만료된 IN_PROGRESS 시험 자동 제출 및 채점 처리
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
