package com.tmk.core.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Document document;

    @Column(name = "chunk_index", nullable = false)
    private short chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static DocumentChunk create(
            Document document,
            short chunkIndex,
            String content,
            float[] embedding,
            OffsetDateTime now
    ) {
        return DocumentChunk.builder()
                .document(document)
                .chunkIndex(chunkIndex)
                .content(content)
                .embedding(embedding)
                .createdAt(now)
                .build();
    }
}
