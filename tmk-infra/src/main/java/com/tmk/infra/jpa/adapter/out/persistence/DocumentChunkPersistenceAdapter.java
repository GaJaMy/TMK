package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.document.entity.DocumentChunk;
import com.tmk.core.port.out.persistence.DocumentChunkPort;
import com.tmk.infra.jpa.repository.DocumentChunkJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentChunkPersistenceAdapter implements DocumentChunkPort {

    private final DocumentChunkJpaRepository documentChunkJpaRepository;

    @Override
    public List<DocumentChunk> saveAll(List<DocumentChunk> documentChunks) {
        return documentChunkJpaRepository.saveAll(documentChunks);
    }

    @Override
    public List<DocumentChunk> findAllByDocumentIdOrderByChunkIndexAsc(Long documentId) {
        return documentChunkJpaRepository.findAllByDocument_IdOrderByChunkIndexAsc(documentId);
    }

    @Override
    public long countByDocumentId(Long documentId) {
        return documentChunkJpaRepository.countByDocument_Id(documentId);
    }
}
