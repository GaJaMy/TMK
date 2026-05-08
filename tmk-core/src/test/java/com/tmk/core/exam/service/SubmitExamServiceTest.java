package com.tmk.core.exam.service;

import com.tmk.core.common.Topic;
import com.tmk.core.common.ContentScope;
import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.ExamPort;
import com.tmk.core.port.out.persistence.QuestionPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionSourceType;
import com.tmk.core.question.entity.QuestionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitExamServiceTest {

    @Mock
    private GetExamService getExamService;

    @Mock
    private ExamPort examPort;

    @Mock
    private QuestionPort questionPort;

    @Mock
    private ExamGradingService examGradingService;

    @InjectMocks
    private SubmitExamService submitExamService;

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

    private Question buildQuestion(Long id) {
        return Question.builder()
                .documentId(1L)
                .ownerUserId(null)
                .scope(ContentScope.PUBLIC)
                .sourceType(QuestionSourceType.PUBLIC_DOCUMENT_GENERATED)
                .content("What is 1+1?")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.EASY)
                .topic(Topic.SPRING)
                .answer("2")
                .explanation("Basic arithmetic")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void submit_inProgress_submitsAndReturnsExam() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExam(ExamStatus.IN_PROGRESS, false);
        Question question = buildQuestion(1L);

        when(getExamService.getExam(examId, userId)).thenReturn(exam);
        when(questionPort.findAllByIds(List.of(1L))).thenReturn(List.of(question));
        when(examGradingService.grade(any(), any())).thenReturn(1);
        when(examPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Exam result = submitExamService.submit(examId, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ExamStatus.SUBMITTED);
    }

    @Test
    void submit_alreadySubmitted_throwsException() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExam(ExamStatus.SUBMITTED, false);
        when(getExamService.getExam(examId, userId)).thenReturn(exam);

        // Act & Assert
        assertThatThrownBy(() -> submitExamService.submit(examId, userId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EXAM_ALREADY_SUBMITTED));
    }

    @Test
    void submit_expired_throwsException() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExam(ExamStatus.IN_PROGRESS, true);
        when(getExamService.getExam(examId, userId)).thenReturn(exam);

        // Act & Assert
        assertThatThrownBy(() -> submitExamService.submit(examId, userId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EXAM_EXPIRED));
    }
}
