package com.tmk.api.admin.question.service;

import com.tmk.api.admin.question.result.AdminPublicQuestionResult;
import com.tmk.api.admin.question.result.AdminPublicQuestionDetailResult;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.PublicQuestionPort;
import com.tmk.core.port.out.persistence.TopicPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.PublicQuestion;
import com.tmk.core.question.entity.PublicQuestionOption;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.vo.PublicQuestionSearchResult;
import com.tmk.core.topic.entity.Topic;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPublicQuestionService {

    private final PublicQuestionPort publicQuestionPort;
    private final TopicPort topicPort;

    @Transactional(readOnly = true)
    public List<AdminPublicQuestionResult> getPublicQuestions(
            Long topicId,
            Difficulty difficulty,
            QuestionType type,
            Boolean active
    ) {
        List<PublicQuestionSearchResult> searchResults = publicQuestionPort.search(topicId, difficulty, type, active);
        return searchResults.stream()
                .map(AdminPublicQuestionResult::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminPublicQuestionDetailResult getPublicQuestion(Long questionId) {
        PublicQuestion publicQuestion = publicQuestionPort.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_QUESTION_NOT_FOUND));

        String topicName = topicPort.findById(publicQuestion.getTopicId())
                .map(Topic::getName)
                .orElse(null);

        return AdminPublicQuestionDetailResult.from(publicQuestion, topicName);
    }

    @Transactional
    public AdminPublicQuestionDetailResult createPublicQuestion(
            Long createdByAdminId,
            Long topicId,
            String content,
            QuestionType type,
            Difficulty difficulty,
            String answer,
            String explanation,
            List<String> options
    ) {
        Topic topic = topicPort.findById(topicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOPIC_NOT_FOUND));
        if (!topic.isActive()) {
            throw new BusinessException(ErrorCode.TOPIC_INACTIVE);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<PublicQuestionOption> questionOptions = createOptions(options);
        PublicQuestion savedQuestion = publicQuestionPort.save(PublicQuestion.create(
                topicId,
                createdByAdminId,
                content,
                type,
                difficulty,
                answer,
                explanation,
                questionOptions,
                now
        ));

        return AdminPublicQuestionDetailResult.from(savedQuestion, topic.getName());
    }

    @Transactional
    public AdminPublicQuestionDetailResult changePublicQuestionStatus(Long questionId, boolean active) {
        PublicQuestion publicQuestion = publicQuestionPort.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_QUESTION_NOT_FOUND));

        OffsetDateTime now = OffsetDateTime.now();
        if (active) {
            publicQuestion.activate(now);
        } else {
            publicQuestion.deactivate(now);
        }

        PublicQuestion savedQuestion = publicQuestionPort.save(publicQuestion);
        String topicName = topicPort.findById(savedQuestion.getTopicId())
                .map(Topic::getName)
                .orElse(null);

        return AdminPublicQuestionDetailResult.from(savedQuestion, topicName);
    }

    @Transactional
    public void changePublicQuestionStatuses(List<Long> questionIds, boolean active) {
        List<Long> distinctQuestionIds = questionIds.stream()
                .distinct()
                .toList();
        List<PublicQuestion> publicQuestions = publicQuestionPort.findAllByIds(distinctQuestionIds);
        if (publicQuestions.size() != distinctQuestionIds.size()) {
            throw new BusinessException(ErrorCode.PUBLIC_QUESTION_NOT_FOUND);
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (PublicQuestion publicQuestion : publicQuestions) {
            if (active) {
                publicQuestion.activate(now);
            } else {
                publicQuestion.deactivate(now);
            }
        }

        publicQuestionPort.saveAll(publicQuestions);
    }

    @Transactional
    public void deletePublicQuestion(Long questionId) {
        publicQuestionPort.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_QUESTION_NOT_FOUND));

        publicQuestionPort.deleteById(questionId);
    }

    @Transactional
    public void deletePublicQuestions(List<Long> questionIds) {
        List<Long> distinctQuestionIds = questionIds.stream()
                .distinct()
                .toList();
        List<PublicQuestion> publicQuestions = publicQuestionPort.findAllByIds(distinctQuestionIds);
        if (publicQuestions.size() != distinctQuestionIds.size()) {
            throw new BusinessException(ErrorCode.PUBLIC_QUESTION_NOT_FOUND);
        }

        publicQuestionPort.deleteAllByIds(distinctQuestionIds);
    }

    private List<PublicQuestionOption> createOptions(List<String> options) {
        List<PublicQuestionOption> questionOptions = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            questionOptions.add(PublicQuestionOption.create((short) (i + 1), options.get(i)));
        }
        return questionOptions;
    }
}
