package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.document.entity.DocumentChunk;
import com.tmk.core.port.out.persistence.DocumentChunkPort;
import com.tmk.infra.jpa.repository.DocumentChunkJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentChunkPersistenceAdapter implements DocumentChunkPort {

    private final DocumentChunkJpaRepository documentChunkJpaRepository;

    @Override
    public List<DocumentChunk> saveAll(List<DocumentChunk> documentChunks) {
        return documentChunkJpaRepository.saveAll(documentChunks);
    }

    @Override
    public long countByDocumentId(Long documentId) {
        return documentChunkJpaRepository.countByDocumentId(documentId);
    }
}
