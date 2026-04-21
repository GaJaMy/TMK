package com.tmk.infra.jpa.repository;

import com.tmk.core.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentChunkJpaRepository extends JpaRepository<DocumentChunk, Long> {

    long countByDocumentId(Long documentId);
}
