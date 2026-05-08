package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.ExamPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveAnswerServiceTest {

    @Mock
    private GetExamService getExamService;

    @Mock
    private ExamPort examPort;

    @InjectMocks
    private SaveAnswerService saveAnswerService;

    private Exam buildExam(ExamStatus status, boolean expired) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiredAt = expired ? now.minusMinutes(1) : now.plusMinutes(30);
        List<ExamQuestion> examQuestions = new ArrayList<>();
        examQuestions.add(ExamQuestion.builder()
                .questionId(1L)
                .orderNum((short) 1)
                .build());
        return Exam.builder()
                .userId(100L)
                .totalQuestions((short) 1)
                .timeLimit((short) 30)
                .status(status)
                .startedAt(now)
                .expiredAt(expiredAt)
                .createdAt(now)
                .examQuestions(examQuestions)
                .build();
    }

    @Test
    void saveAnswers_inProgress_savesAnswers() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExam(ExamStatus.IN_PROGRESS, false);
        when(getExamService.getExam(examId, userId)).thenReturn(exam);
        when(examPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        saveAnswerService.saveAnswers(examId, userId, Map.of(1L, "A"));

        // Assert
        verify(examPort).save(exam);
    }

    @Test
    void saveAnswers_alreadySubmitted_throwsException() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExam(ExamStatus.SUBMITTED, false);
        when(getExamService.getExam(examId, userId)).thenReturn(exam);

        // Act & Assert
        assertThatThrownBy(() -> saveAnswerService.saveAnswers(examId, userId, Map.of(1L, "A")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EXAM_ALREADY_SUBMITTED));

        verify(examPort, never()).save(any());
    }

    @Test
    void saveAnswers_expired_throwsException() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExam(ExamStatus.IN_PROGRESS, true);
        when(getExamService.getExam(examId, userId)).thenReturn(exam);

        // Act & Assert
        assertThatThrownBy(() -> saveAnswerService.saveAnswers(examId, userId, Map.of(1L, "A")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EXAM_EXPIRED));

        verify(examPort, never()).save(any());
    }
}
