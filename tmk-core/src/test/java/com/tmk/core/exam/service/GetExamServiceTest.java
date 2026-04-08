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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExamServiceTest {

    @Mock
    private ExamPort examPort;

    @InjectMocks
    private GetExamService getExamService;

    private Exam buildExam(Long id, Long userId) {
        OffsetDateTime now = OffsetDateTime.now();
        return Exam.builder()
                .userId(userId)
                .totalQuestions((short) 10)
                .timeLimit((short) 30)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now)
                .expiredAt(now.plusMinutes(30))
                .createdAt(now)
                .build();
    }

    @Test
    void getExam_found_returnsExam() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExam(examId, userId);
        when(examPort.findByIdAndUserId(examId, userId)).thenReturn(Optional.of(exam));

        // Act
        Exam result = getExamService.getExam(examId, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    void getExam_notFound_throwsBusinessException() {
        // Arrange
        Long examId = 99L;
        Long userId = 100L;
        when(examPort.findByIdAndUserId(examId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getExamService.getExam(examId, userId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EXAM_NOT_FOUND));
    }
}
