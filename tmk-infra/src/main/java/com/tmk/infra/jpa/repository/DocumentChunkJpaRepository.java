package com.tmk.infra.jpa.repository;

import com.tmk.core.document.entity.DocumentChunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentChunkJpaRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findAllByDocument_IdOrderByChunkIndexAsc(Long documentId);

    long countByDocument_Id(Long documentId);
}
