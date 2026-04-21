package com.tmk.api.exam.usecase;

import com.tmk.api.exam.dto.*;
import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.exam.service.*;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamUseCaseTest {

    @Mock
    private CreateExamService createExamService;

    @Mock
    private GetExamService getExamService;

    @Mock
    private SaveAnswerService saveAnswerService;

    @Mock
    private SubmitExamService submitExamService;

    @Mock
    private GetExamResultService getExamResultService;

    @Mock
    private GetExamHistoryService getExamHistoryService;

    @Mock
    private GetExamHistoryDetailService getExamHistoryDetailService;

    @Mock
    private QuestionPort questionPort;

    @InjectMocks
    private ExamUseCase examUseCase;

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

    private Exam buildExamWithQuestion(ExamStatus status, boolean isCorrect) {
        OffsetDateTime now = OffsetDateTime.now();
        List<ExamQuestion> examQuestions = new ArrayList<>();
        ExamQuestion eq = ExamQuestion.builder()
                .questionId(1L)
                .orderNum((short) 1)
                .build();
        eq.grade(isCorrect);
        examQuestions.add(eq);
        return Exam.builder()
                .userId(100L)
                .totalQuestions((short) 1)
                .timeLimit((short) 30)
                .status(status)
                .startedAt(now.minusMinutes(5))
                .expiredAt(now.plusMinutes(25))
                .submittedAt(status == ExamStatus.SUBMITTED ? now : null)
                .createdAt(now.minusMinutes(5))
                .examQuestions(examQuestions)
                .build();
    }

    private Question buildQuestion(Long id) {
        OffsetDateTime now = OffsetDateTime.now();
        return Question.builder()
                .documentId(1L)
                .ownerUserId(null)
                .scope(ContentScope.PUBLIC)
                .sourceType(QuestionSourceType.PUBLIC_DOCUMENT_GENERATED)
                .content("Sample question?")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.EASY)
                .topic(Topic.SPRING)
                .answer("answer")
                .explanation("explanation here")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void create_returnsExamResult() {
        // Arrange
        Exam exam = buildExam(ExamStatus.IN_PROGRESS);
        when(createExamService.create(100L, ContentScope.PUBLIC, null)).thenReturn(exam);

        // Act
        ExamResult result = examUseCase.create(100L, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalQuestions()).isEqualTo(10);
        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void saveAnswers_convertsAnswerCommandsToMap() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        List<AnswerCommand> answers = List.of(new AnswerCommand(1L, "A"), new AnswerCommand(2L, "B"));

        // Act (no exception means success)
        examUseCase.saveAnswers(examId, userId, answers);
    }

    @Test
    void submit_returnsSubmitResult() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExamWithQuestion(ExamStatus.SUBMITTED, true);
        when(submitExamService.submit(examId, userId)).thenReturn(exam);

        // Act
        SubmitResult result = examUseCase.submit(examId, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCorrectCount()).isEqualTo(1);
        assertThat(result.getTotalQuestions()).isEqualTo(1);
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void getResult_returnsExamResultData() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExamWithQuestion(ExamStatus.SUBMITTED, true);
        when(getExamResultService.getResult(examId, userId)).thenReturn(exam);

        // Act
        ExamResultData result = examUseCase.getResult(examId, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCorrectCount()).isEqualTo(1);
        assertThat(result.getScore()).isEqualTo(100.0);
    }

    @Test
    void getHistory_returnsHistoryListResult() {
        // Arrange
        Long userId = 100L;
        List<Exam> exams = List.of(buildExamWithQuestion(ExamStatus.SUBMITTED, true));
        when(getExamHistoryService.getHistory(userId, 0, 20)).thenReturn(exams);
        when(getExamHistoryService.count(userId)).thenReturn(1L);

        // Act
        HistoryListResult result = examUseCase.getHistory(userId, 0, 20);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void getHistoryDetail_returnsHistoryDetailResult() {
        // Arrange
        Long examId = 1L;
        Long userId = 100L;
        Exam exam = buildExamWithQuestion(ExamStatus.SUBMITTED, true);
        Question question = buildQuestion(1L);
        when(getExamHistoryDetailService.getHistoryDetail(examId, userId)).thenReturn(exam);
        when(questionPort.findById(anyLong())).thenReturn(Optional.of(question));

        // Act
        HistoryDetailResult result = examUseCase.getHistoryDetail(examId, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getQuestions()).hasSize(1);
        assertThat(result.isPassed()).isTrue();
        assertThat(result.getScore()).isEqualTo(100.0);
    }
}
