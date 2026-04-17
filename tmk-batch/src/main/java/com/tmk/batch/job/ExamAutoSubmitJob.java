package com.tmk.batch.job;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.service.ExamGradingService;
import com.tmk.core.port.out.ExamPort;
import com.tmk.core.port.out.QuestionPort;
import com.tmk.core.question.entity.Question;
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
import java.util.List;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class ExamAutoSubmitJob {

    private static final Logger log = LoggerFactory.getLogger(ExamAutoSubmitJob.class);

    private final ExamPort examPort;
    private final QuestionPort questionPort;
    private final ExamGradingService examGradingService;

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
                    List<Exam> expiredExams = examPort.findExpiredInProgressExams(OffsetDateTime.now());
                    int processedCount = 0;

                    for (Exam exam : expiredExams) {
                        try {
                            List<Long> questionIds = exam.getExamQuestions().stream()
                                    .map(eq -> eq.getQuestionId())
                                    .toList();

                            List<Question> questions = questionIds.stream()
                                    .map(id -> questionPort.findById(id))
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .toList();

                            examGradingService.grade(exam, questions);
                            exam.submit();
                            examPort.save(exam);
                            processedCount++;
                        } catch (Exception e) {
                            log.warn("Failed to auto-submit exam id={}: {}", exam.getId(), e.getMessage());
                        }
                    }

                    log.info("ExamAutoSubmitJob completed: processed {} expired exams", processedCount);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
