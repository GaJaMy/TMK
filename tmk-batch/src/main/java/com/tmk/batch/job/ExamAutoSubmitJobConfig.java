package com.tmk.batch.job;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.port.out.persistence.ExamPort;
import com.tmk.core.port.out.persistence.PrivateQuestionPort;
import com.tmk.core.port.out.persistence.PublicQuestionPort;
import com.tmk.core.question.entity.PrivateQuestion;
import com.tmk.core.question.entity.PublicQuestion;
import com.tmk.core.question.entity.QuestionScope;
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

@Configuration
@RequiredArgsConstructor
public class ExamAutoSubmitJobConfig {

    private static final Logger log = LoggerFactory.getLogger(ExamAutoSubmitJobConfig.class);

    private final ExamPort examPort;
    private final PublicQuestionPort publicQuestionPort;
    private final PrivateQuestionPort privateQuestionPort;

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
                            gradeExamQuestions(exam);
                            exam.submit(OffsetDateTime.now());
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

    private void gradeExamQuestions(Exam exam) {
        List<ExamQuestion> examQuestions = exam.getExamQuestions();
        for (ExamQuestion examQuestion : examQuestions) {
            boolean correct = switch (examQuestion.getQuestionScope()) {
                case PUBLIC -> isPublicQuestionCorrect(examQuestion);
                case PRIVATE -> isPrivateQuestionCorrect(examQuestion);
            };
            examQuestion.grade(correct);
        }
    }

    private boolean isPublicQuestionCorrect(ExamQuestion examQuestion) {
        PublicQuestion publicQuestion = publicQuestionPort.findById(examQuestion.getPublicQuestionId())
                .orElse(null);
        return publicQuestion != null && isAnswerCorrect(examQuestion.getMyAnswer(), publicQuestion.getAnswer());
    }

    private boolean isPrivateQuestionCorrect(ExamQuestion examQuestion) {
        PrivateQuestion privateQuestion = privateQuestionPort.findById(examQuestion.getPrivateQuestionId())
                .orElse(null);
        return privateQuestion != null && isAnswerCorrect(examQuestion.getMyAnswer(), privateQuestion.getAnswer());
    }

    private boolean isAnswerCorrect(String myAnswer, String answer) {
        if (myAnswer == null || answer == null) {
            return false;
        }
        return myAnswer.trim().equalsIgnoreCase(answer.trim());
    }
}
