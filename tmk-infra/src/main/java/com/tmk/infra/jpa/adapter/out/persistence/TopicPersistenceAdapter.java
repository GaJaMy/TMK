package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.port.out.persistence.TopicPort;
import com.tmk.core.topic.entity.Topic;
import com.tmk.infra.jpa.repository.PublicQuestionJpaRepository;
import com.tmk.infra.jpa.repository.TopicJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopicPersistenceAdapter implements TopicPort {

    private final TopicJpaRepository topicJpaRepository;
    private final PublicQuestionJpaRepository publicQuestionJpaRepository;

    @Override
    public Topic save(Topic topic) {
        return topicJpaRepository.save(topic);
    }

    @Override
    public Optional<Topic> findById(Long topicId) {
        return topicJpaRepository.findById(topicId);
    }

    @Override
    public Optional<Topic> findByName(String name) {
        return topicJpaRepository.findByName(name);
    }

    @Override
    public List<Topic> findAll() {
        return topicJpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Topic> findAllActive() {
        return topicJpaRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public boolean existsByName(String name) {
        return topicJpaRepository.existsByName(name);
    }

    @Override
    public boolean existsPublicQuestionByTopicId(Long topicId) {
        return publicQuestionJpaRepository.existsByTopicId(topicId);
    }

    @Override
    public void deleteById(Long topicId) {
        topicJpaRepository.deleteById(topicId);
    }
}
