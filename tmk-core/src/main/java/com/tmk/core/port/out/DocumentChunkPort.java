package com.tmk.core.port.out;

import com.tmk.core.document.entity.DocumentChunk;

import java.util.List;

public interface DocumentChunkPort {

    List<DocumentChunk> findByDocumentId(Long documentId);

    List<DocumentChunk> saveAll(List<DocumentChunk> chunks);

    long countByDocumentId(Long documentId);
}
