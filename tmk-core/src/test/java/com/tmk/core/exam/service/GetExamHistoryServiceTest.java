package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.port.out.persistence.ExamPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExamHistoryServiceTest {

    @Mock
    private ExamPort examPort;

    @InjectMocks
    private GetExamHistoryService getExamHistoryService;

    private Exam buildExam() {
        OffsetDateTime now = OffsetDateTime.now();
        return Exam.builder()
                .userId(100L)
                .totalQuestions((short) 10)
                .timeLimit((short) 30)
                .status(ExamStatus.SUBMITTED)
                .startedAt(now)
                .expiredAt(now.plusMinutes(30))
                .createdAt(now)
                .build();
    }

    @Test
    void getHistory_firstPage_returnsPaginatedList() {
        // Arrange
        Long userId = 100L;
        List<Exam> allExams = List.of(buildExam(), buildExam(), buildExam());
        when(examPort.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(allExams);

        // Act
        List<Exam> result = getExamHistoryService.getHistory(userId, 0, 2);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getHistory_secondPage_returnsRemainingItems() {
        // Arrange
        Long userId = 100L;
        List<Exam> allExams = List.of(buildExam(), buildExam(), buildExam());
        when(examPort.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(allExams);

        // Act
        List<Exam> result = getExamHistoryService.getHistory(userId, 1, 2);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void getHistory_pageOutOfBounds_returnsEmptyList() {
        // Arrange
        Long userId = 100L;
        List<Exam> allExams = List.of(buildExam(), buildExam());
        when(examPort.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(allExams);

        // Act
        List<Exam> result = getExamHistoryService.getHistory(userId, 5, 10);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void count_returnsTotal() {
        // Arrange
        Long userId = 100L;
        List<Exam> allExams = List.of(buildExam(), buildExam(), buildExam());
        when(examPort.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(allExams);

        // Act
        long count = getExamHistoryService.count(userId);

        // Assert
        assertThat(count).isEqualTo(3);
    }
}
