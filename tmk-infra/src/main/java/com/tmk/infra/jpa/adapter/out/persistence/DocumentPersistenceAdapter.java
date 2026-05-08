package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.document.entity.Document;
import com.tmk.core.port.out.persistence.DocumentPort;
import com.tmk.infra.jpa.repository.DocumentJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements DocumentPort {

    private final DocumentJpaRepository documentJpaRepository;

    @Override
    public Document save(Document document) {
        return documentJpaRepository.save(document);
    }

    @Override
    public Optional<Document> findById(Long documentId) {
        return documentJpaRepository.findById(documentId);
    }

    @Override
    public Optional<Document> findByIdAndUserId(Long documentId, Long userId) {
        return documentJpaRepository.findByIdAndUserId(documentId, userId);
    }

    @Override
    public List<Document> findAllByUserIdOrderByCreatedAtDesc(Long userId) {
        return documentJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
}
