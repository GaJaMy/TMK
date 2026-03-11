package com.tmk.core.document.repository;

import com.tmk.core.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentId(Long documentId);
}
