package com.tmk.api.admin.topic.service;

import com.tmk.api.admin.topic.result.AdminTopicResult;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.TopicPort;
import com.tmk.core.topic.entity.Topic;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTopicService {

    private final TopicPort topicPort;

    @Transactional
    public AdminTopicResult createTopic(Long createdByAdminId, String name) {
        if (topicPort.existsByName(name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_TOPIC_NAME);
        }

        OffsetDateTime now = OffsetDateTime.now();
        return AdminTopicResult.from(topicPort.save(Topic.create(name, null, createdByAdminId, now)));
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        topicPort.findById(topicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOPIC_NOT_FOUND));

        if (topicPort.existsPublicQuestionByTopicId(topicId)) {
            throw new BusinessException(ErrorCode.TOPIC_HAS_PUBLIC_QUESTIONS);
        }

        topicPort.deleteById(topicId);
    }
}
