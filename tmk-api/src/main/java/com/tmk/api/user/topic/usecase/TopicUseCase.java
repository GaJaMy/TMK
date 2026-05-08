package com.tmk.api.user.topic.usecase;

import com.tmk.api.user.topic.dto.TopicResponse;
import com.tmk.api.user.topic.result.TopicResult;
import com.tmk.api.user.topic.service.TopicService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TopicUseCase {

    private final TopicService topicService;

    @Transactional(readOnly = true)
    public List<TopicResponse> getTopics() {
        List<TopicResult> results = topicService.getTopics();
        return results.stream()
                .map(TopicResponse::from)
                .toList();
    }
}
