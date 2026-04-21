package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.common.ContentScope;
import com.tmk.core.document.entity.Document;
import com.tmk.core.port.out.persistence.DocumentPort;
import com.tmk.infra.jpa.repository.DocumentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements DocumentPort {

    private final DocumentJpaRepository documentJpaRepository;

    @Override
    public Optional<Document> findById(Long id) {
        return documentJpaRepository.findById(id);
    }

    @Override
    public Optional<Document> findPrivateByIdAndOwnerUserId(Long id, Long ownerUserId) {
        return documentJpaRepository.findByIdAndScopeAndOwnerUserId(id, ContentScope.PRIVATE, ownerUserId);
    }

    @Override
    public Document save(Document document) {
        return documentJpaRepository.save(document);
    }
}
