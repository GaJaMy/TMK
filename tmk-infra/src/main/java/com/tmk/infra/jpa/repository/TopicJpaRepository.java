package com.tmk.infra.jpa.repository;

import com.tmk.core.topic.entity.Topic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicJpaRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByName(String name);

    boolean existsByName(String name);

    List<Topic> findAllByOrderByCreatedAtDesc();

    List<Topic> findByActiveTrueOrderByCreatedAtDesc();
}
