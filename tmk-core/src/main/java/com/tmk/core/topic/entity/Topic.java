package com.tmk.core.topic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "topic")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_by_admin_id", nullable = false)
    private Long createdByAdminId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static Topic create(String name, String description, Long createdByAdminId, OffsetDateTime now) {
        return Topic.builder()
                .name(name)
                .description(description)
                .active(true)
                .createdByAdminId(createdByAdminId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void deactivate(OffsetDateTime now) {
        this.active = false;
        this.updatedAt = now;
    }

    public void activate(OffsetDateTime now) {
        this.active = true;
        this.updatedAt = now;
    }

    public void changeDescription(String description, OffsetDateTime now) {
        this.description = description;
        this.updatedAt = now;
    }
}
