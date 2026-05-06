package com.tmk.api.user.topic.service;

import com.tmk.api.user.topic.result.TopicResult;
import com.tmk.core.port.out.persistence.TopicPort;
import com.tmk.core.topic.entity.Topic;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicPort topicPort;

    @Transactional(readOnly = true)
    public List<TopicResult> getTopics() {
        List<Topic> topics = topicPort.findAllActive();
        return topics.stream()
                .map(TopicResult::from)
                .toList();
    }
}
