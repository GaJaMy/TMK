package com.tmk.core.port.out.persistence;

import com.tmk.core.topic.entity.Topic;
import java.util.List;
import java.util.Optional;

public interface TopicPort {

    Topic save(Topic topic);

    Optional<Topic> findById(Long topicId);

    Optional<Topic> findByName(String name);

    List<Topic> findAll();

    List<Topic> findAllActive();

    boolean existsByName(String name);

    boolean existsPublicQuestionByTopicId(Long topicId);

    void deleteById(Long topicId);
}
