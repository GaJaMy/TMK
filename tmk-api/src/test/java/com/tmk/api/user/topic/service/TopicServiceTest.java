package com.tmk.api.user.topic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.tmk.api.user.topic.result.TopicResult;
import com.tmk.core.port.out.persistence.TopicPort;
import com.tmk.core.topic.entity.Topic;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicPort topicPort;

    @InjectMocks
    private TopicService topicService;

    @Test
    void getTopicsReturnsActiveTopicResults() {
        OffsetDateTime now = OffsetDateTime.parse("2026-05-06T09:00:00+09:00");
        Topic topic = Topic.builder()
                .id(1L)
                .name("SPRING")
                .description("Spring Framework and Spring Boot")
                .active(true)
                .createdByAdminId(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<Topic> topics = List.of(topic);
        given(topicPort.findAllActive()).willReturn(topics);

        List<TopicResult> results = topicService.getTopics();

        assertThat(results).containsExactly(
                new TopicResult(1L, "SPRING", "Spring Framework and Spring Boot", true)
        );
    }
}
