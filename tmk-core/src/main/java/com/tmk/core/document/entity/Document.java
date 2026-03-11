package com.tmk.core.document.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "document")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 500)
    private String source;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public void updateStatus(DocumentStatus status) {
        this.status = status;
    }
}
