package com.tmk.core.document.repository;

import com.tmk.core.document.entity.DocumentChunk;
import com.tmk.core.port.out.DocumentChunkPort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long>, DocumentChunkPort {

    List<DocumentChunk> findByDocumentId(Long documentId);

    long countByDocumentId(Long documentId);
}
