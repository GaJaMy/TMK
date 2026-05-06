package com.tmk.api.user.exam.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.tmk.api.user.exam.result.ExamCreateResult;
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
