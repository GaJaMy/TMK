package com.tmk.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(BatchScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job examAutoSubmitJob;
    private final Job expiredVerificationCleanJob;

    @Scheduled(fixedRate = 60000)
    public void runExamAutoSubmit() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(examAutoSubmitJob, params);
        } catch (Exception e) {
            log.error("ExamAutoSubmitJob execution failed", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void runExpiredVerificationClean() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(expiredVerificationCleanJob, params);
        } catch (Exception e) {
            log.error("ExpiredVerificationCleanJob execution failed", e);
        }
    }
}
