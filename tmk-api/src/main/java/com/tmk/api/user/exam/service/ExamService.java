package com.tmk.api.user.exam.service;

import com.tmk.api.user.exam.result.ExamCreateResult;
import com.tmk.api.user.exam.result.ExamSummaryResult;
import com.tmk.api.user.exam.result.ExamStartResult;
import com.tmk.core.document.entity.Document;
import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.DocumentPort;
import com.tmk.core.port.out.persistence.ExamPort;
import com.tmk.core.port.out.persistence.PrivateQuestionPort;
import com.tmk.core.port.out.persistence.PublicQuestionPort;
import com.tmk.core.port.out.persistence.TopicPort;
import com.tmk.core.question.entity.PrivateQuestion;
import com.tmk.core.question.entity.PublicQuestion;
import com.tmk.core.topic.entity.Topic;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public List<ExamSummaryResult> getExams(Long userId) {
        List<Exam> exams = examPort.findAvailableByUserIdOrderByCreatedAtDesc(userId);
        OffsetDateTime now = OffsetDateTime.now();
        return exams.stream()
                .map(exam -> ExamSummaryResult.of(exam, resolveExamTitle(userId, exam), now))
                .toList();
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
                        autoSubmitExpiredExam(inProgressExam, now);
                        return;
                    }
                    throw new BusinessException(ErrorCode.EXAM_IN_PROGRESS_ALREADY_EXISTS);
                });

        exam.start(now);
        Exam savedExam = examPort.save(exam);
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

    private void autoSubmitExpiredExam(Exam exam, OffsetDateTime now) {
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
