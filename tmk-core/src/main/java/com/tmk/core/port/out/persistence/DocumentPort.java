package com.tmk.core.port.out.persistence;

import com.tmk.core.document.entity.Document;
import java.util.List;
import java.util.Optional;

public interface DocumentPort {

    Document save(Document document);

    Optional<Document> findById(Long documentId);

    Optional<Document> findByIdAndUserId(Long documentId, Long userId);

    List<Document> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
