package com.tmk.core.port.out.persistence;

import com.tmk.core.document.entity.DocumentChunk;
import java.util.List;

public interface DocumentChunkPort {

    List<DocumentChunk> saveAll(List<DocumentChunk> documentChunks);

    List<DocumentChunk> findAllByDocumentIdOrderByChunkIndexAsc(Long documentId);

    long countByDocumentId(Long documentId);
}
