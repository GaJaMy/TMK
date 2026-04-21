package com.tmk.core.document.entity;

import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
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

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ContentScope scope;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Topic topic;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public void updateStatus(DocumentStatus status) {
        this.status = status;
    }
}
