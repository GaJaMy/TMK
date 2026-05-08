package com.tmk.api.user.exam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import com.tmk.api.monitoring.event.ExamStartedEvent;
import com.tmk.api.user.exam.command.ExamAnswerSaveCommand;
import com.tmk.api.user.exam.result.ExamCreateResult;
import com.tmk.api.user.exam.result.ExamDetailResult;
import com.tmk.api.user.exam.result.ExamResultDetailResult;
import com.tmk.api.user.exam.result.ExamSummaryResult;
import com.tmk.api.user.exam.result.ExamStartResult;
import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentSourceType;
import com.tmk.core.document.entity.DocumentStatus;
import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.DocumentPort;
import com.tmk.core.port.out.persistence.ExamPort;
import com.tmk.core.port.out.persistence.PrivateQuestionPort;
import com.tmk.core.port.out.persistence.PublicQuestionPort;
import com.tmk.core.port.out.persistence.TopicPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.PrivateQuestion;
import com.tmk.core.question.entity.PublicQuestion;
import com.tmk.core.question.entity.PublicQuestionOption;
import com.tmk.core.question.entity.QuestionScope;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.topic.entity.Topic;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamPort examPort;

    @Mock
    private TopicPort topicPort;

    @Mock
    private DocumentPort documentPort;

    @Mock
    private PublicQuestionPort publicQuestionPort;

    @Mock
    private PrivateQuestionPort privateQuestionPort;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ExamService examService;

    @Test
    void createPublicTopicExamCreatesCreatedExam() {
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .active(true)
                .build();
        List<PublicQuestion> questions = List.of(
                buildPublicQuestion(11L, 1L),
                buildPublicQuestion(12L, 1L)
        );
        given(topicPort.findById(1L)).willReturn(Optional.of(topic));
        given(publicQuestionPort.countActiveByTopicId(1L)).willReturn(2L);
        given(publicQuestionPort.findAllActiveByTopicIdLimit(1L, 2)).willReturn(questions);
        given(examPort.save(any(Exam.class))).willAnswer(invocation -> invocation.getArgument(0));

        ExamCreateResult result = examService.createPublicTopicExam(7L, 1L, (short) 2, (short) 30);

        assertThat(result.sourceType()).isEqualTo(ExamSourceType.PUBLIC_TOPIC);
        assertThat(result.totalQuestions()).isEqualTo((short) 2);
        assertThat(result.timeLimitMinutes()).isEqualTo((short) 30);
        assertThat(result.status()).isEqualTo(ExamStatus.CREATED);
    }

    @Test
    void createPrivateDocumentExamCreatesCreatedExam() {
        OffsetDateTime now = OffsetDateTime.now();
        Document document = Document.builder()
                .id(11L)
                .userId(7L)
                .title("OS Notes")
                .sourceType(DocumentSourceType.MD_UPLOAD)
                .sourceReference("/tmp/os-notes.md")
                .status(DocumentStatus.COMPLETED)
                .generatedQuestionCount(3)
                .createdAt(now.minusHours(1))
                .updatedAt(now)
                .build();
        List<PrivateQuestion> questions = List.of(
                buildPrivateQuestion(21L, 11L),
                buildPrivateQuestion(22L, 11L)
        );
        given(documentPort.findByIdAndUserId(11L, 7L)).willReturn(Optional.of(document));
        given(privateQuestionPort.countByDocumentId(11L)).willReturn(3L);
        given(privateQuestionPort.findAllByDocumentIdLimit(11L, 2)).willReturn(questions);
        given(examPort.save(any(Exam.class))).willAnswer(invocation -> invocation.getArgument(0));

        ExamCreateResult result = examService.createPrivateDocumentExam(7L, 11L, (short) 2, (short) 20);

        assertThat(result.sourceType()).isEqualTo(ExamSourceType.PRIVATE_DOCUMENT);
        assertThat(result.totalQuestions()).isEqualTo((short) 2);
        assertThat(result.timeLimitMinutes()).isEqualTo((short) 20);
        assertThat(result.status()).isEqualTo(ExamStatus.CREATED);
    }

    @Test
    void createPrivateDocumentExamThrowsWhenDocumentIsNotCompleted() {
        OffsetDateTime now = OffsetDateTime.now();
        Document document = Document.builder()
                .id(11L)
                .userId(7L)
                .title("OS Notes")
                .sourceType(DocumentSourceType.MD_UPLOAD)
                .sourceReference("/tmp/os-notes.md")
                .status(DocumentStatus.PROCESSING)
                .generatedQuestionCount(0)
                .createdAt(now.minusHours(1))
                .updatedAt(now)
                .build();
        given(documentPort.findByIdAndUserId(11L, 7L)).willReturn(Optional.of(document));

        assertThatThrownBy(() -> examService.createPrivateDocumentExam(
                7L,
                11L,
                (short) 2,
                (short) 20
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DOCUMENT_NOT_READY.getMessage());
    }

    @Test
    void createPublicTopicExamThrowsWhenQuestionCountIsNotEnough() {
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .active(true)
                .build();
        given(topicPort.findById(1L)).willReturn(Optional.of(topic));
        given(publicQuestionPort.countActiveByTopicId(1L)).willReturn(1L);

        assertThatThrownBy(() -> examService.createPublicTopicExam(
                7L,
                1L,
                (short) 2,
                (short) 30
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PUBLIC_QUESTION_NOT_ENOUGH.getMessage());
    }

    @Test
    void getExamsReturnsCreatedAndInProgressExams() {
        OffsetDateTime now = OffsetDateTime.now();
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .active(true)
                .build();
        Document document = Document.builder()
                .id(11L)
                .userId(7L)
                .title("운영체제 정리.md")
                .sourceType(DocumentSourceType.MD_UPLOAD)
                .sourceReference("/tmp/os-notes.md")
                .status(DocumentStatus.COMPLETED)
                .generatedQuestionCount(12)
                .createdAt(now.minusHours(2))
                .updatedAt(now.minusHours(1))
                .build();
        Exam inProgressExam = Exam.builder()
                .id(101L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 10)
                .timeLimitMinutes((short) 30)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(5))
                .expiredAt(now.plusMinutes(25))
                .createdAt(now.minusMinutes(10))
                .examQuestions(List.of(ExamQuestion.createPublic((short) 1, 11L)))
                .build();
        Exam createdExam = Exam.builder()
                .id(102L)
                .userId(7L)
                .documentId(11L)
                .sourceType(ExamSourceType.PRIVATE_DOCUMENT)
                .totalQuestions((short) 12)
                .timeLimitMinutes((short) 20)
                .status(ExamStatus.CREATED)
                .createdAt(now.minusMinutes(2))
                .examQuestions(List.of(ExamQuestion.createPrivate((short) 1, 21L)))
                .build();
        given(topicPort.findById(1L)).willReturn(Optional.of(topic));
        given(documentPort.findByIdAndUserId(11L, 7L)).willReturn(Optional.of(document));
        given(examPort.findAvailableByUserIdOrderByCreatedAtDesc(7L))
                .willReturn(List.of(createdExam, inProgressExam));

        List<ExamSummaryResult> results = examService.getExams(7L);

        assertThat(results).hasSize(2);
        assertThat(results.getFirst().examId()).isEqualTo(102L);
        assertThat(results.getFirst().title()).isEqualTo("운영체제 정리.md - 시험");
        assertThat(results.getFirst().status()).isEqualTo(ExamStatus.CREATED);
        assertThat(results.get(1).examId()).isEqualTo(101L);
        assertThat(results.get(1).title()).isEqualTo("Spring - 시험");
        assertThat(results.get(1).remainingSeconds()).isPositive();
    }

    @Test
    void getExamsReturnsEmptyListWhenThereIsNoAvailableExam() {
        given(examPort.findAvailableByUserIdOrderByCreatedAtDesc(7L)).willReturn(List.of());

        List<ExamSummaryResult> results = examService.getExams(7L);

        assertThat(results).isEmpty();
    }

    @Test
    void getExamReturnsInProgressExamQuestions() {
        OffsetDateTime now = OffsetDateTime.now();
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .active(true)
                .build();
        ExamQuestion examQuestion = ExamQuestion.builder()
                .id(1001L)
                .publicQuestionId(11L)
                .questionScope(QuestionScope.PUBLIC)
                .orderNum((short) 1)
                .myAnswer("Bean")
                .build();
        PublicQuestion publicQuestion = PublicQuestion.builder()
                .id(11L)
                .topicId(1L)
                .createdByAdminId(1L)
                .content("Spring Container가 관리하는 객체를 무엇이라고 하나요?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .difficulty(Difficulty.NORMAL)
                .answer("Bean")
                .explanation("설명")
                .active(true)
                .options(List.of(
                        PublicQuestionOption.create((short) 1, "Bean"),
                        PublicQuestionOption.create((short) 2, "Entity"),
                        PublicQuestionOption.create((short) 3, "Repository"),
                        PublicQuestionOption.create((short) 4, "Service"),
                        PublicQuestionOption.create((short) 5, "Component")
                ))
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusHours(1))
                .build();
        Exam exam = Exam.builder()
                .id(101L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 1)
                .timeLimitMinutes((short) 30)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(5))
                .expiredAt(now.plusMinutes(25))
                .createdAt(now.minusMinutes(10))
                .examQuestions(List.of(examQuestion))
                .build();
        given(examPort.findByIdAndUserId(101L, 7L)).willReturn(Optional.of(exam));
        given(topicPort.findById(1L)).willReturn(Optional.of(topic));
        given(publicQuestionPort.findById(11L)).willReturn(Optional.of(publicQuestion));

        ExamDetailResult result = examService.getExam(7L, 101L);

        assertThat(result.examId()).isEqualTo(101L);
        assertThat(result.title()).isEqualTo("Spring - 시험");
        assertThat(result.questions()).hasSize(1);
        assertThat(result.questions().getFirst().examQuestionId()).isEqualTo(1001L);
        assertThat(result.questions().getFirst().options()).hasSize(5);
        assertThat(result.questions().getFirst().myAnswer()).isEqualTo("Bean");
    }

    @Test
    void getExamAutoSubmitsExpiredExamAndThrowsExpired() {
        OffsetDateTime now = OffsetDateTime.now();
        ExamQuestion examQuestion = ExamQuestion.builder()
                .id(1002L)
                .privateQuestionId(21L)
                .questionScope(QuestionScope.PRIVATE)
                .orderNum((short) 1)
                .myAnswer("answer")
                .build();
        Exam exam = Exam.builder()
                .id(101L)
                .userId(7L)
                .documentId(11L)
                .sourceType(ExamSourceType.PRIVATE_DOCUMENT)
                .totalQuestions((short) 1)
                .timeLimitMinutes((short) 10)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(20))
                .expiredAt(now.minusMinutes(10))
                .createdAt(now.minusMinutes(25))
                .examQuestions(List.of(examQuestion))
                .build();
        PrivateQuestion privateQuestion = PrivateQuestion.builder()
                .id(21L)
                .userId(7L)
                .documentId(11L)
                .content("private question")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.NORMAL)
                .answer("answer")
                .explanation("explanation")
                .languageCode("KR")
                .options(List.of())
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusHours(1))
                .build();
        given(examPort.findByIdAndUserId(101L, 7L)).willReturn(Optional.of(exam));
        given(privateQuestionPort.findById(21L)).willReturn(Optional.of(privateQuestion));
        given(examPort.save(any(Exam.class))).willAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> examService.getExam(7L, 101L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.EXAM_EXPIRED.getMessage());

        verify(examPort).save(exam);
        assertThat(exam.getStatus()).isEqualTo(ExamStatus.SUBMITTED);
    }

    @Test
    void saveAnswersStoresMyAnswer() {
        OffsetDateTime now = OffsetDateTime.now();
        ExamQuestion examQuestion = ExamQuestion.builder()
                .id(1003L)
                .publicQuestionId(11L)
                .questionScope(QuestionScope.PUBLIC)
                .orderNum((short) 1)
                .build();
        Exam exam = Exam.builder()
                .id(101L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 1)
                .timeLimitMinutes((short) 30)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(5))
                .expiredAt(now.plusMinutes(25))
                .createdAt(now.minusMinutes(10))
                .examQuestions(List.of(examQuestion))
                .build();
        PublicQuestion publicQuestion = PublicQuestion.builder()
                .id(11L)
                .topicId(1L)
                .createdByAdminId(1L)
                .content("public question")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.NORMAL)
                .answer("Bean")
                .explanation("explanation")
                .active(true)
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusHours(1))
                .build();
        given(examPort.findByIdAndUserId(101L, 7L)).willReturn(Optional.of(exam));
        given(publicQuestionPort.findById(11L)).willReturn(Optional.of(publicQuestion));
        given(examPort.save(any(Exam.class))).willAnswer(invocation -> invocation.getArgument(0));

        examService.saveAnswers(7L, 101L, List.of(new ExamAnswerSaveCommand(1003L, "Bean")));

        verify(examPort).save(exam);
        assertThat(exam.getExamQuestions().getFirst().getMyAnswer()).isEqualTo("Bean");
    }

    @Test
    void saveAnswersAutoSubmitsExpiredExamAndThrowsExpired() {
        OffsetDateTime now = OffsetDateTime.now();
        ExamQuestion examQuestion = ExamQuestion.builder()
                .id(1004L)
                .privateQuestionId(21L)
                .questionScope(QuestionScope.PRIVATE)
                .orderNum((short) 1)
                .myAnswer("before")
                .build();
        Exam exam = Exam.builder()
                .id(101L)
                .userId(7L)
                .documentId(11L)
                .sourceType(ExamSourceType.PRIVATE_DOCUMENT)
                .totalQuestions((short) 1)
                .timeLimitMinutes((short) 10)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(20))
                .expiredAt(now.minusMinutes(10))
                .createdAt(now.minusMinutes(25))
                .examQuestions(List.of(examQuestion))
                .build();
        PrivateQuestion privateQuestion = PrivateQuestion.builder()
                .id(21L)
                .userId(7L)
                .documentId(11L)
                .content("private question")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.NORMAL)
                .answer("before")
                .explanation("explanation")
                .languageCode("KR")
                .options(List.of())
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusHours(1))
                .build();
        given(examPort.findByIdAndUserId(101L, 7L)).willReturn(Optional.of(exam));
        given(privateQuestionPort.findById(21L)).willReturn(Optional.of(privateQuestion));
        given(examPort.save(any(Exam.class))).willAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> examService.saveAnswers(
                7L,
                101L,
                List.of(new ExamAnswerSaveCommand(1004L, "after"))
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.EXAM_EXPIRED.getMessage());

        verify(examPort).save(exam);
        assertThat(exam.getStatus()).isEqualTo(ExamStatus.SUBMITTED);
        assertThat(exam.getExamQuestions().getFirst().getMyAnswer()).isEqualTo("before");
    }

    @Test
    void submitExamGradesAndSubmitsInProgressExam() {
        OffsetDateTime now = OffsetDateTime.now();
        ExamQuestion examQuestion = ExamQuestion.builder()
                .id(1005L)
                .publicQuestionId(11L)
                .questionScope(QuestionScope.PUBLIC)
                .orderNum((short) 1)
                .myAnswer("Bean")
                .build();
        Exam exam = Exam.builder()
                .id(101L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 1)
                .timeLimitMinutes((short) 30)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(5))
                .expiredAt(now.plusMinutes(25))
                .createdAt(now.minusMinutes(10))
                .examQuestions(List.of(examQuestion))
                .build();
        PublicQuestion publicQuestion = PublicQuestion.builder()
                .id(11L)
                .topicId(1L)
                .createdByAdminId(1L)
                .content("public question")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.NORMAL)
                .answer("Bean")
                .explanation("explanation")
                .active(true)
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusHours(1))
                .build();
        given(examPort.findByIdAndUserId(101L, 7L)).willReturn(Optional.of(exam));
        given(publicQuestionPort.findById(11L)).willReturn(Optional.of(publicQuestion));
        given(examPort.save(any(Exam.class))).willAnswer(invocation -> invocation.getArgument(0));

        examService.submitExam(7L, 101L);

        verify(examPort).save(exam);
        assertThat(exam.getStatus()).isEqualTo(ExamStatus.SUBMITTED);
        assertThat(exam.getSubmittedAt()).isNotNull();
        assertThat(exam.getExamQuestions().getFirst().getCorrect()).isTrue();
    }

    @Test
    void submitExamAutoSubmitsExpiredExamAndThrowsExpired() {
        OffsetDateTime now = OffsetDateTime.now();
        ExamQuestion examQuestion = ExamQuestion.builder()
                .id(1006L)
                .privateQuestionId(21L)
                .questionScope(QuestionScope.PRIVATE)
                .orderNum((short) 1)
                .myAnswer("before")
                .build();
        Exam exam = Exam.builder()
                .id(101L)
                .userId(7L)
                .documentId(11L)
                .sourceType(ExamSourceType.PRIVATE_DOCUMENT)
                .totalQuestions((short) 1)
                .timeLimitMinutes((short) 10)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(20))
                .expiredAt(now.minusMinutes(10))
                .createdAt(now.minusMinutes(25))
                .examQuestions(List.of(examQuestion))
                .build();
        PrivateQuestion privateQuestion = PrivateQuestion.builder()
                .id(21L)
                .userId(7L)
                .documentId(11L)
                .content("private question")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.NORMAL)
                .answer("before")
                .explanation("explanation")
                .languageCode("KR")
                .options(List.of())
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusHours(1))
                .build();
        given(examPort.findByIdAndUserId(101L, 7L)).willReturn(Optional.of(exam));
        given(privateQuestionPort.findById(21L)).willReturn(Optional.of(privateQuestion));
        given(examPort.save(any(Exam.class))).willAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> examService.submitExam(7L, 101L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.EXAM_EXPIRED.getMessage());

        verify(examPort).save(exam);
        assertThat(exam.getStatus()).isEqualTo(ExamStatus.SUBMITTED);
        assertThat(exam.getSubmittedAt()).isNotNull();
    }

    @Test
    void getExamResultReturnsSubmittedExamResult() {
        OffsetDateTime now = OffsetDateTime.now();
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .active(true)
                .build();
        ExamQuestion correctQuestion = ExamQuestion.builder()
                .id(1007L)
                .publicQuestionId(11L)
                .questionScope(QuestionScope.PUBLIC)
                .orderNum((short) 1)
                .myAnswer("Bean")
                .correct(true)
                .build();
        ExamQuestion wrongQuestion = ExamQuestion.builder()
                .id(1008L)
                .publicQuestionId(12L)
                .questionScope(QuestionScope.PUBLIC)
                .orderNum((short) 2)
                .myAnswer("Repository")
                .correct(false)
                .build();
        Exam exam = Exam.builder()
                .id(101L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 2)
                .timeLimitMinutes((short) 30)
                .status(ExamStatus.SUBMITTED)
                .startedAt(now.minusMinutes(30))
                .expiredAt(now.minusMinutes(5))
                .submittedAt(now.minusMinutes(4))
                .createdAt(now.minusMinutes(35))
                .examQuestions(List.of(correctQuestion, wrongQuestion))
                .build();
        PublicQuestion firstQuestion = PublicQuestion.builder()
                .id(11L)
                .topicId(1L)
                .createdByAdminId(1L)
                .content("첫 번째 문제")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.NORMAL)
                .answer("Bean")
                .explanation("첫 번째 해설")
                .active(true)
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusHours(1))
                .build();
        PublicQuestion secondQuestion = PublicQuestion.builder()
                .id(12L)
                .topicId(1L)
                .createdByAdminId(1L)
                .content("두 번째 문제")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.HARD)
                .answer("Component")
                .explanation("두 번째 해설")
                .active(true)
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusHours(1))
                .build();
        given(examPort.findByIdAndUserId(101L, 7L)).willReturn(Optional.of(exam));
        given(topicPort.findById(1L)).willReturn(Optional.of(topic));
        given(publicQuestionPort.findById(11L)).willReturn(Optional.of(firstQuestion));
        given(publicQuestionPort.findById(12L)).willReturn(Optional.of(secondQuestion));

        ExamResultDetailResult result = examService.getExamResult(7L, 101L);

        assertThat(result.title()).isEqualTo("Spring - 시험");
        assertThat(result.summary().correctCount()).isEqualTo(1);
        assertThat(result.summary().wrongCount()).isEqualTo(1);
        assertThat(result.summary().score()).isEqualTo(50);
        assertThat(result.questions()).hasSize(2);
        assertThat(result.questions().getFirst().correct()).isTrue();
        assertThat(result.questions().get(1).correctAnswer()).isEqualTo("Component");
    }

    @Test
    void getExamResultThrowsWhenExamIsNotSubmitted() {
        OffsetDateTime now = OffsetDateTime.now();
        Exam exam = Exam.builder()
                .id(101L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 2)
                .timeLimitMinutes((short) 30)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(10))
                .expiredAt(now.plusMinutes(20))
                .createdAt(now.minusMinutes(15))
                .examQuestions(List.of())
                .build();
        given(examPort.findByIdAndUserId(101L, 7L)).willReturn(Optional.of(exam));

        assertThatThrownBy(() -> examService.getExamResult(7L, 101L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.EXAM_RESULT_NOT_READY.getMessage());
    }

    @Test
    void getExamHistoryReturnsSubmittedExamSummaries() {
        OffsetDateTime now = OffsetDateTime.now();
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .active(true)
                .build();
        Document document = Document.builder()
                .id(11L)
                .userId(7L)
                .title("운영체제 정리.md")
                .sourceType(DocumentSourceType.MD_UPLOAD)
                .sourceReference("/tmp/os.md")
                .status(DocumentStatus.COMPLETED)
                .generatedQuestionCount(10)
                .createdAt(now.minusDays(1))
                .updatedAt(now.minusHours(1))
                .build();
        Exam submittedPublicExam = Exam.builder()
                .id(101L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 2)
                .timeLimitMinutes((short) 30)
                .status(ExamStatus.SUBMITTED)
                .startedAt(now.minusMinutes(40))
                .expiredAt(now.minusMinutes(10))
                .submittedAt(now.minusMinutes(8))
                .createdAt(now.minusMinutes(45))
                .examQuestions(List.of(
                        ExamQuestion.builder()
                                .id(1009L)
                                .publicQuestionId(11L)
                                .questionScope(QuestionScope.PUBLIC)
                                .orderNum((short) 1)
                                .correct(true)
                                .build(),
                        ExamQuestion.builder()
                                .id(1010L)
                                .publicQuestionId(12L)
                                .questionScope(QuestionScope.PUBLIC)
                                .orderNum((short) 2)
                                .correct(false)
                                .build()
                ))
                .build();
        Exam submittedPrivateExam = Exam.builder()
                .id(102L)
                .userId(7L)
                .documentId(11L)
                .sourceType(ExamSourceType.PRIVATE_DOCUMENT)
                .totalQuestions((short) 1)
                .timeLimitMinutes((short) 20)
                .status(ExamStatus.SUBMITTED)
                .startedAt(now.minusMinutes(80))
                .expiredAt(now.minusMinutes(50))
                .submittedAt(now.minusMinutes(49))
                .createdAt(now.minusMinutes(85))
                .examQuestions(List.of(
                        ExamQuestion.builder()
                                .id(1011L)
                                .privateQuestionId(21L)
                                .questionScope(QuestionScope.PRIVATE)
                                .orderNum((short) 1)
                                .correct(true)
                                .build()
                ))
                .build();
        Exam createdExam = Exam.builder()
                .id(103L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 5)
                .timeLimitMinutes((short) 10)
                .status(ExamStatus.CREATED)
                .createdAt(now.minusMinutes(5))
                .examQuestions(List.of())
                .build();
        given(examPort.findHistoryByUserIdOrderByCreatedAtDesc(7L))
                .willReturn(List.of(createdExam, submittedPrivateExam, submittedPublicExam));
        given(topicPort.findById(1L)).willReturn(Optional.of(topic));
        given(documentPort.findByIdAndUserId(11L, 7L)).willReturn(Optional.of(document));

        var results = examService.getExamHistory(7L);

        assertThat(results).hasSize(2);
        assertThat(results.getFirst().examId()).isEqualTo(102L);
        assertThat(results.getFirst().title()).isEqualTo("운영체제 정리.md - 시험");
        assertThat(results.getFirst().correctCount()).isEqualTo(1);
        assertThat(results.getFirst().score()).isEqualTo(100);
        assertThat(results.getFirst().pass()).isTrue();
        assertThat(results.get(1).examId()).isEqualTo(101L);
        assertThat(results.get(1).title()).isEqualTo("Spring - 시험");
        assertThat(results.get(1).score()).isEqualTo(50);
        assertThat(results.get(1).pass()).isFalse();
    }

    @Test
    void getExamHistoryReturnsEmptyListWhenThereIsNoSubmittedExam() {
        OffsetDateTime now = OffsetDateTime.now();
        Exam createdExam = Exam.builder()
                .id(103L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 5)
                .timeLimitMinutes((short) 10)
                .status(ExamStatus.CREATED)
                .createdAt(now.minusMinutes(5))
                .examQuestions(List.of())
                .build();
        given(examPort.findHistoryByUserIdOrderByCreatedAtDesc(7L)).willReturn(List.of(createdExam));

        var results = examService.getExamHistory(7L);

        assertThat(results).isEmpty();
    }

    @Test
    void startExamStartsCreatedExamImmediately() {
        OffsetDateTime now = OffsetDateTime.now();
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .active(true)
                .build();
        Exam exam = Exam.builder()
                .id(101L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 10)
                .timeLimitMinutes((short) 30)
                .status(ExamStatus.CREATED)
                .createdAt(now.minusMinutes(5))
                .examQuestions(List.of(ExamQuestion.createPublic((short) 1, 11L)))
                .build();
        given(examPort.findByIdAndUserId(101L, 7L)).willReturn(Optional.of(exam));
        given(examPort.findInProgressByUserId(7L)).willReturn(Optional.empty());
        given(examPort.save(any(Exam.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(topicPort.findById(1L)).willReturn(Optional.of(topic));

        ExamStartResult result = examService.startExam(7L, 101L);

        assertThat(result.examId()).isEqualTo(101L);
        assertThat(result.title()).isEqualTo("Spring - 시험");
        assertThat(result.status()).isEqualTo(ExamStatus.IN_PROGRESS);
        assertThat(result.startedAt()).isNotNull();
        assertThat(result.expiredAt()).isEqualTo(result.startedAt().plusMinutes(30));
        assertThat(result.remainingSeconds()).isPositive();
        then(applicationEventPublisher).should().publishEvent(new ExamStartedEvent(7L));
    }

    @Test
    void startExamThrowsWhenAnotherExamIsAlreadyInProgress() {
        OffsetDateTime now = OffsetDateTime.now();
        Exam targetExam = Exam.builder()
                .id(101L)
                .userId(7L)
                .topicId(1L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 10)
                .timeLimitMinutes((short) 30)
                .status(ExamStatus.CREATED)
                .createdAt(now.minusMinutes(5))
                .examQuestions(List.of(ExamQuestion.createPublic((short) 1, 11L)))
                .build();
        Exam inProgressExam = Exam.builder()
                .id(102L)
                .userId(7L)
                .topicId(2L)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions((short) 10)
                .timeLimitMinutes((short) 20)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now.minusMinutes(10))
                .expiredAt(now.plusMinutes(10))
                .createdAt(now.minusMinutes(15))
                .examQuestions(List.of(ExamQuestion.createPublic((short) 1, 12L)))
                .build();
        given(examPort.findByIdAndUserId(101L, 7L)).willReturn(Optional.of(targetExam));
        given(examPort.findInProgressByUserId(7L)).willReturn(Optional.of(inProgressExam));

        assertThatThrownBy(() -> examService.startExam(7L, 101L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.EXAM_IN_PROGRESS_ALREADY_EXISTS.getMessage());
    }

    private PublicQuestion buildPublicQuestion(Long questionId, Long topicId) {
        OffsetDateTime now = OffsetDateTime.now();
        return PublicQuestion.builder()
                .id(questionId)
                .topicId(topicId)
                .createdByAdminId(1L)
                .content("public question")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.NORMAL)
                .answer("answer")
                .explanation("explanation")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private PrivateQuestion buildPrivateQuestion(Long questionId, Long documentId) {
        OffsetDateTime now = OffsetDateTime.now();
        return PrivateQuestion.builder()
                .id(questionId)
                .userId(7L)
                .documentId(documentId)
                .content("private question")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.NORMAL)
                .answer("answer")
                .explanation("explanation")
                .languageCode("KR")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
