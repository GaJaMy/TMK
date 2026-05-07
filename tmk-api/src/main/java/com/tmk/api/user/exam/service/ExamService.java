package com.tmk.api.user.exam.service;

import com.tmk.api.monitoring.event.ExamStartedEvent;
import com.tmk.api.question.support.QuestionAnswerSupport;
import com.tmk.api.question.support.QuestionAnswerSupport.QuestionOptionCandidate;
import com.tmk.api.user.exam.command.ExamAnswerSaveCommand;
import com.tmk.api.user.exam.result.ExamCreateResult;
import com.tmk.api.user.exam.result.ExamDetailResult;
import com.tmk.api.user.exam.result.ExamHistorySummaryResult;
import com.tmk.api.user.exam.result.ExamQuestionDetailResult;
import com.tmk.api.user.exam.result.ExamQuestionOptionResult;
import com.tmk.api.user.exam.result.ExamResultDetailResult;
import com.tmk.api.user.exam.result.ExamResultQuestionResult;
import com.tmk.api.user.exam.result.ExamResultSummaryResult;
import com.tmk.api.user.exam.result.ExamSummaryResult;
import com.tmk.api.user.exam.result.ExamStartResult;
import com.tmk.core.document.entity.Document;
import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.DocumentPort;
import com.tmk.core.port.out.persistence.ExamPort;
import com.tmk.core.port.out.persistence.PrivateQuestionPort;
import com.tmk.core.port.out.persistence.PublicQuestionPort;
import com.tmk.core.port.out.persistence.TopicPort;
import com.tmk.core.question.entity.PrivateQuestion;
import com.tmk.core.question.entity.PrivateQuestionOption;
import com.tmk.core.question.entity.PublicQuestion;
import com.tmk.core.question.entity.PublicQuestionOption;
import com.tmk.core.topic.entity.Topic;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

    private final ExamPort examPort;
    private final TopicPort topicPort;
    private final DocumentPort documentPort;
    private final PublicQuestionPort publicQuestionPort;
    private final PrivateQuestionPort privateQuestionPort;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public List<ExamSummaryResult> getExams(Long userId) {
        List<Exam> exams = examPort.findAvailableByUserIdOrderByCreatedAtDesc(userId);
        OffsetDateTime now = OffsetDateTime.now();
        return exams.stream()
                .map(exam -> ExamSummaryResult.of(exam, resolveExamTitle(userId, exam), now))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamHistorySummaryResult> getExamHistory(Long userId) {
        List<Exam> exams = examPort.findHistoryByUserIdOrderByCreatedAtDesc(userId);
        List<Exam> submittedExams = exams.stream()
                .filter(exam -> exam.getStatus() == ExamStatus.SUBMITTED)
                .toList();
        return submittedExams.stream()
                .map(exam -> {
                    ExamResultSummaryResult summary = buildExamResultSummary(exam);
                    boolean pass = summary.score() >= 60;
                    return ExamHistorySummaryResult.of(
                            exam,
                            resolveExamTitle(userId, exam),
                            summary.correctCount(),
                            summary.score(),
                            pass
                    );
                })
                .toList();
    }

    @Transactional
    public ExamDetailResult getExam(Long userId, Long examId) {
        Exam exam = examPort.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        OffsetDateTime now = OffsetDateTime.now();
        if (exam.isInProgress() && exam.isExpired(now)) {
            completeExam(exam, now);
            throw new BusinessException(ErrorCode.EXAM_EXPIRED);
        }

        exam.validateInProgress(now);
        List<ExamQuestionDetailResult> questionResults = buildExamQuestionResults(exam);
        return ExamDetailResult.of(exam, resolveExamTitle(userId, exam), now, questionResults);
    }

    @Transactional(readOnly = true)
    public ExamResultDetailResult getExamResult(Long userId, Long examId) {
        Exam exam = examPort.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
        if (exam.getStatus() != ExamStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.EXAM_RESULT_NOT_READY);
        }

        List<ExamResultQuestionResult> questionResults = buildExamResultQuestionResults(exam);
        ExamResultSummaryResult summary = buildExamResultSummary(exam);
        return ExamResultDetailResult.of(exam, resolveExamTitle(userId, exam), summary, questionResults);
    }

    @Transactional
    public void saveAnswers(Long userId, Long examId, List<ExamAnswerSaveCommand> commands) {
        Exam exam = examPort.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        OffsetDateTime now = OffsetDateTime.now();
        if (exam.isInProgress() && exam.isExpired(now)) {
            completeExam(exam, now);
            throw new BusinessException(ErrorCode.EXAM_EXPIRED);
        }

        for (ExamAnswerSaveCommand command : commands) {
            String normalizedAnswer = normalizeSubmittedAnswer(exam, command.questionId(), command.answer());
            exam.saveAnswer(command.questionId(), normalizedAnswer, now);
        }
        examPort.save(exam);
    }

    @Transactional
    public void submitExam(Long userId, Long examId) {
        Exam exam = examPort.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        OffsetDateTime now = OffsetDateTime.now();
        if (exam.isInProgress() && exam.isExpired(now)) {
            completeExam(exam, now);
            throw new BusinessException(ErrorCode.EXAM_EXPIRED);
        }

        completeExam(exam, now);
    }

    @Transactional
    public ExamStartResult startExam(Long userId, Long examId) {
        Exam exam = examPort.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        OffsetDateTime now = OffsetDateTime.now();
        examPort.findInProgressByUserId(userId)
                .filter(inProgressExam -> !inProgressExam.getId().equals(examId))
                .ifPresent(inProgressExam -> {
                    if (inProgressExam.isExpired(now)) {
                        completeExam(inProgressExam, now);
                        return;
                    }
                    throw new BusinessException(ErrorCode.EXAM_IN_PROGRESS_ALREADY_EXISTS);
                });

        exam.start(now);
        Exam savedExam = examPort.save(exam);
        applicationEventPublisher.publishEvent(new ExamStartedEvent(userId));
        return ExamStartResult.of(savedExam, resolveExamTitle(userId, savedExam), now);
    }

    @Transactional
    public ExamCreateResult createPublicTopicExam(
            Long userId,
            Long topicId,
            short questionCount,
            short timeLimitMinutes
    ) {
        Topic topic = topicPort.findById(topicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOPIC_NOT_FOUND));
        if (!topic.isActive()) {
            throw new BusinessException(ErrorCode.TOPIC_INACTIVE);
        }

        long activeQuestionCount = publicQuestionPort.countActiveByTopicId(topicId);
        if (activeQuestionCount < questionCount) {
            throw new BusinessException(ErrorCode.PUBLIC_QUESTION_NOT_ENOUGH);
        }

        List<PublicQuestion> questions = publicQuestionPort.findAllActiveByTopicIdLimit(topicId, questionCount);
        if (questions.size() < questionCount) {
            throw new BusinessException(ErrorCode.PUBLIC_QUESTION_NOT_ENOUGH);
        }

        OffsetDateTime now = OffsetDateTime.now();
        Exam exam = Exam.createPublicTopicExam(userId, topicId, questionCount, timeLimitMinutes, now);
        attachPublicQuestions(exam, questions);
        Exam savedExam = examPort.save(exam);
        return ExamCreateResult.from(savedExam);
    }

    @Transactional
    public ExamCreateResult createPrivateDocumentExam(
            Long userId,
            Long documentId,
            short questionCount,
            short timeLimitMinutes
    ) {
        Document document = documentPort.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));
        document.validateReadyForExam();

        long privateQuestionCount = privateQuestionPort.countByDocumentId(documentId);
        if (privateQuestionCount < questionCount) {
            throw new BusinessException(ErrorCode.PRIVATE_QUESTION_NOT_ENOUGH);
        }

        List<PrivateQuestion> questions = privateQuestionPort.findAllByDocumentIdLimit(documentId, questionCount);
        if (questions.size() < questionCount) {
            throw new BusinessException(ErrorCode.PRIVATE_QUESTION_NOT_ENOUGH);
        }

        OffsetDateTime now = OffsetDateTime.now();
        Exam exam = Exam.createPrivateDocumentExam(userId, documentId, questionCount, timeLimitMinutes, now);
        attachPrivateQuestions(exam, questions);
        Exam savedExam = examPort.save(exam);
        return ExamCreateResult.from(savedExam);
    }

    private void attachPublicQuestions(Exam exam, List<PublicQuestion> questions) {
        for (int index = 0; index < questions.size(); index++) {
            exam.addQuestion(ExamQuestion.createPublic((short) (index + 1), questions.get(index).getId()));
        }
    }

    private void attachPrivateQuestions(Exam exam, List<PrivateQuestion> questions) {
        for (int index = 0; index < questions.size(); index++) {
            exam.addQuestion(ExamQuestion.createPrivate((short) (index + 1), questions.get(index).getId()));
        }
    }

    private List<ExamQuestionDetailResult> buildExamQuestionResults(Exam exam) {
        List<ExamQuestion> examQuestions = exam.getExamQuestions();
        return examQuestions.stream()
                .map(this::buildExamQuestionResult)
                .toList();
    }

    private List<ExamResultQuestionResult> buildExamResultQuestionResults(Exam exam) {
        List<ExamQuestion> examQuestions = exam.getExamQuestions();
        return examQuestions.stream()
                .map(this::buildExamResultQuestionResult)
                .toList();
    }

    private ExamQuestionDetailResult buildExamQuestionResult(ExamQuestion examQuestion) {
        return switch (examQuestion.getQuestionScope()) {
            case PUBLIC -> buildPublicExamQuestionResult(examQuestion);
            case PRIVATE -> buildPrivateExamQuestionResult(examQuestion);
        };
    }

    private ExamResultQuestionResult buildExamResultQuestionResult(ExamQuestion examQuestion) {
        return switch (examQuestion.getQuestionScope()) {
            case PUBLIC -> buildPublicExamResultQuestionResult(examQuestion);
            case PRIVATE -> buildPrivateExamResultQuestionResult(examQuestion);
        };
    }

    private ExamQuestionDetailResult buildPublicExamQuestionResult(ExamQuestion examQuestion) {
        PublicQuestion publicQuestion = publicQuestionPort.findById(examQuestion.getPublicQuestionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_QUESTION_NOT_FOUND));
        List<PublicQuestionOption> publicQuestionOptions = publicQuestion.getOptions();
        List<ExamQuestionOptionResult> options = publicQuestionOptions.stream()
                .sorted(Comparator.comparing(PublicQuestionOption::getOptionNumber))
                .map(option -> new ExamQuestionOptionResult(option.getOptionNumber(), option.getContent()))
                .toList();
        return new ExamQuestionDetailResult(
                examQuestion.getId(),
                examQuestion.getOrderNum(),
                publicQuestion.getContent(),
                publicQuestion.getType(),
                publicQuestion.getDifficulty(),
                options,
                examQuestion.getMyAnswer()
        );
    }

    private ExamQuestionDetailResult buildPrivateExamQuestionResult(ExamQuestion examQuestion) {
        PrivateQuestion privateQuestion = privateQuestionPort.findById(examQuestion.getPrivateQuestionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRIVATE_QUESTION_NOT_FOUND));
        List<PrivateQuestionOption> privateQuestionOptions = privateQuestion.getOptions();
        List<ExamQuestionOptionResult> options = privateQuestionOptions.stream()
                .sorted(Comparator.comparing(PrivateQuestionOption::getOptionNumber))
                .map(option -> new ExamQuestionOptionResult(option.getOptionNumber(), option.getContent()))
                .toList();
        return new ExamQuestionDetailResult(
                examQuestion.getId(),
                examQuestion.getOrderNum(),
                privateQuestion.getContent(),
                privateQuestion.getType(),
                privateQuestion.getDifficulty(),
                options,
                examQuestion.getMyAnswer()
        );
    }

    private ExamResultQuestionResult buildPublicExamResultQuestionResult(ExamQuestion examQuestion) {
        PublicQuestion publicQuestion = publicQuestionPort.findById(examQuestion.getPublicQuestionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_QUESTION_NOT_FOUND));
        List<PublicQuestionOption> publicQuestionOptions = publicQuestion.getOptions();
        List<ExamQuestionOptionResult> options = publicQuestionOptions.stream()
                .sorted(Comparator.comparing(PublicQuestionOption::getOptionNumber))
                .map(option -> new ExamQuestionOptionResult(option.getOptionNumber(), option.getContent()))
                .toList();
        List<QuestionOptionCandidate> candidates = toQuestionOptionCandidates(options);
        return new ExamResultQuestionResult(
                examQuestion.getId(),
                examQuestion.getOrderNum(),
                publicQuestion.getContent(),
                publicQuestion.getType(),
                publicQuestion.getDifficulty(),
                options,
                QuestionAnswerSupport.toDisplayAnswer(publicQuestion.getType(), examQuestion.getMyAnswer(), candidates),
                QuestionAnswerSupport.toDisplayAnswer(publicQuestion.getType(), publicQuestion.getAnswer(), candidates),
                Boolean.TRUE.equals(examQuestion.getCorrect()),
                publicQuestion.getExplanation()
        );
    }

    private ExamResultQuestionResult buildPrivateExamResultQuestionResult(ExamQuestion examQuestion) {
        PrivateQuestion privateQuestion = privateQuestionPort.findById(examQuestion.getPrivateQuestionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRIVATE_QUESTION_NOT_FOUND));
        List<PrivateQuestionOption> privateQuestionOptions = privateQuestion.getOptions();
        List<ExamQuestionOptionResult> options = privateQuestionOptions.stream()
                .sorted(Comparator.comparing(PrivateQuestionOption::getOptionNumber))
                .map(option -> new ExamQuestionOptionResult(option.getOptionNumber(), option.getContent()))
                .toList();
        List<QuestionOptionCandidate> candidates = toQuestionOptionCandidates(options);
        return new ExamResultQuestionResult(
                examQuestion.getId(),
                examQuestion.getOrderNum(),
                privateQuestion.getContent(),
                privateQuestion.getType(),
                privateQuestion.getDifficulty(),
                options,
                QuestionAnswerSupport.toDisplayAnswer(privateQuestion.getType(), examQuestion.getMyAnswer(), candidates),
                QuestionAnswerSupport.toDisplayAnswer(privateQuestion.getType(), privateQuestion.getAnswer(), candidates),
                Boolean.TRUE.equals(examQuestion.getCorrect()),
                privateQuestion.getExplanation()
        );
    }

    private ExamResultSummaryResult buildExamResultSummary(Exam exam) {
        List<ExamQuestion> examQuestions = exam.getExamQuestions();
        List<ExamQuestion> questions = examQuestions;
        int correctCount = (int) questions.stream()
                .filter(question -> Boolean.TRUE.equals(question.getCorrect()))
                .count();
        int wrongCount = questions.size() - correctCount;
        int score = questions.isEmpty() ? 0 : Math.round((correctCount * 100.0f) / questions.size());
        return new ExamResultSummaryResult(correctCount, wrongCount, score);
    }

    private String resolveExamTitle(Long userId, Exam exam) {
        return switch (exam.getSourceType()) {
            case PUBLIC_TOPIC -> resolvePublicExamTitle(exam.getTopicId());
            case PRIVATE_DOCUMENT -> resolvePrivateExamTitle(userId, exam.getDocumentId());
        };
    }

    private String resolvePublicExamTitle(Long topicId) {
        Topic topic = topicPort.findById(topicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOPIC_NOT_FOUND));
        return topic.getName() + " - 시험";
    }

    private String resolvePrivateExamTitle(Long userId, Long documentId) {
        Document document = documentPort.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));
        return document.getTitle() + " - 시험";
    }

    private void completeExam(Exam exam, OffsetDateTime now) {
        gradeExamQuestions(exam);
        exam.submit(now);
        examPort.save(exam);
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
        return publicQuestion != null
                && QuestionAnswerSupport.isCorrectAnswer(
                publicQuestion.getType(),
                examQuestion.getMyAnswer(),
                publicQuestion.getAnswer()
        );
    }

    private boolean isPrivateQuestionCorrect(ExamQuestion examQuestion) {
        PrivateQuestion privateQuestion = privateQuestionPort.findById(examQuestion.getPrivateQuestionId())
                .orElse(null);
        return privateQuestion != null
                && QuestionAnswerSupport.isCorrectAnswer(
                privateQuestion.getType(),
                examQuestion.getMyAnswer(),
                privateQuestion.getAnswer()
        );
    }

    private String normalizeSubmittedAnswer(Exam exam, Long examQuestionId, String answer) {
        ExamQuestion examQuestion = exam.getExamQuestions().stream()
                .filter(candidate -> candidate.getId().equals(examQuestionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_QUESTION_NOT_FOUND));
        return switch (examQuestion.getQuestionScope()) {
            case PUBLIC -> normalizePublicQuestionAnswer(examQuestion.getPublicQuestionId(), answer);
            case PRIVATE -> normalizePrivateQuestionAnswer(examQuestion.getPrivateQuestionId(), answer);
        };
    }

    private String normalizePublicQuestionAnswer(Long questionId, String answer) {
        PublicQuestion publicQuestion = publicQuestionPort.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_QUESTION_NOT_FOUND));
        List<QuestionOptionCandidate> candidates = publicQuestion.getOptions().stream()
                .map(option -> new QuestionOptionCandidate(option.getOptionNumber(), option.getContent()))
                .toList();
        return QuestionAnswerSupport.normalizeStoredAnswer(publicQuestion.getType(), answer, candidates);
    }

    private String normalizePrivateQuestionAnswer(Long questionId, String answer) {
        PrivateQuestion privateQuestion = privateQuestionPort.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRIVATE_QUESTION_NOT_FOUND));
        List<QuestionOptionCandidate> candidates = privateQuestion.getOptions().stream()
                .map(option -> new QuestionOptionCandidate(option.getOptionNumber(), option.getContent()))
                .toList();
        return QuestionAnswerSupport.normalizeStoredAnswer(privateQuestion.getType(), answer, candidates);
    }

    private List<QuestionOptionCandidate> toQuestionOptionCandidates(List<ExamQuestionOptionResult> options) {
        return options.stream()
                .map(option -> new QuestionOptionCandidate(option.optionNumber(), option.content()))
                .toList();
    }

}
