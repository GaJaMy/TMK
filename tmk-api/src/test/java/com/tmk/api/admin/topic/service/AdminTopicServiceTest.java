package com.tmk.api.admin.topic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.tmk.api.admin.topic.result.AdminTopicResult;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.PublicQuestionPort;
import com.tmk.core.port.out.persistence.TopicPort;
import com.tmk.core.topic.entity.Topic;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AdminTopicServiceTest {

    @Mock
    private TopicPort topicPort;

    @Mock
    private PublicQuestionPort publicQuestionPort;

    @InjectMocks
    private AdminTopicService adminTopicService;

    @Test
    void createTopicSavesNewTopic() {
        OffsetDateTime now = OffsetDateTime.now();
        Topic savedTopic = Topic.builder()
                .id(1L)
                .name("SPRING")
                .description(null)
                .active(true)
                .createdByAdminId(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(topicPort.existsByName("SPRING")).willReturn(false);
        given(topicPort.save(any(Topic.class))).willReturn(savedTopic);

        AdminTopicResult result = adminTopicService.createTopic(101L, "SPRING");

        assertThat(result).isEqualTo(new AdminTopicResult(1L, "SPRING", true, 0L, now));
        then(topicPort).should().existsByName("SPRING");
        then(topicPort).should().save(any(Topic.class));
    }

    @Test
    void createTopicThrowsWhenNameAlreadyExists() {
        given(topicPort.existsByName("SPRING")).willReturn(true);

        assertThatThrownBy(() -> adminTopicService.createTopic(101L, "SPRING"))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DUPLICATE_TOPIC_NAME.getMessage());
    }

    @Test
    void deleteTopicDeletesWhenTopicExistsAndHasNoPublicQuestions() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        Topic topic = Topic.builder()
                .id(1L)
                .name("SPRING")
                .description(null)
                .active(true)
                .createdByAdminId(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(topicPort.findById(1L)).willReturn(Optional.of(topic));
        given(topicPort.existsPublicQuestionByTopicId(1L)).willReturn(false);

        adminTopicService.deleteTopic(1L);

        then(topicPort).should().deleteById(1L);
    }

    @Test
    void deleteTopicThrowsWhenTopicDoesNotExist() {
        given(topicPort.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminTopicService.deleteTopic(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.TOPIC_NOT_FOUND.getMessage());
    }

    @Test
    void deleteTopicThrowsWhenPublicQuestionsAreConnected() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        Topic topic = Topic.builder()
                .id(1L)
                .name("SPRING")
                .description(null)
                .active(true)
                .createdByAdminId(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(topicPort.findById(1L)).willReturn(Optional.of(topic));
        given(topicPort.existsPublicQuestionByTopicId(1L)).willReturn(true);

        assertThatThrownBy(() -> adminTopicService.deleteTopic(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.TOPIC_HAS_PUBLIC_QUESTIONS.getMessage());
    }
}
