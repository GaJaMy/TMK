package com.tmk.core.document.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "document_chunk")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "chunk_index", nullable = false)
    private Short chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // pgvector: 별도 네이티브 쿼리로 처리
    @Column(columnDefinition = "vector(1536)")
    private String embedding;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
