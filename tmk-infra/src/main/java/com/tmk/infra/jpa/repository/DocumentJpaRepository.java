package com.tmk.infra.jpa.repository;

import com.tmk.core.document.entity.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentJpaRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByIdAndUserId(Long documentId, Long userId);

    List<Document> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
