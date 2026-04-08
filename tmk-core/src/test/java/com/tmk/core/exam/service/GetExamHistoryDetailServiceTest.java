package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ExamPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExamHistoryDetailServiceTest {

    @Mock
    private GetExamService getExamService;

    @Mock
    private ExamPort examPort;

    @InjectMocks
    private GetExamHistoryDetailService getExamHistoryDetailService;

    private Exam buildExam(ExamStatus status) {
        OffsetDateTime now = OffsetDateTime.now();
        return Exam.builder()
                .userId(100L)
                .totalQuestions((short) 10)
                .timeLimit((short) 30)
                .status(status)
                .startedAt(now)
                .expiredAt(now.plusMinutes(30))
                .createdAt(now)
                .build();
    }

    @Test
    void getHistoryDetail_submittedExam_returnsExam() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExam(ExamStatus.SUBMITTED);
        when(getExamService.getExam(examId, userId)).thenReturn(exam);

        // Act
        Exam result = getExamHistoryDetailService.getHistoryDetail(examId, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ExamStatus.SUBMITTED);
    }

    @Test
    void getHistoryDetail_expiredExam_returnsExam() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExam(ExamStatus.EXPIRED);
        when(getExamService.getExam(examId, userId)).thenReturn(exam);

        // Act
        Exam result = getExamHistoryDetailService.getHistoryDetail(examId, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ExamStatus.EXPIRED);
    }

    @Test
    void getHistoryDetail_inProgressExam_throwsException() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExam(ExamStatus.IN_PROGRESS);
        when(getExamService.getExam(examId, userId)).thenReturn(exam);

        // Act & Assert
        assertThatThrownBy(() -> getExamHistoryDetailService.getHistoryDetail(examId, userId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EXAM_NOT_IN_PROGRESS));
    }
}
