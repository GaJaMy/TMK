package com.tmk.core.admin.entity;

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
@Table(name = "admin")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_by_admin_id")
    private Long createdByAdminId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static AdminAccount create(
            String username,
            String password,
            Long createdByAdminId,
            OffsetDateTime now
    ) {
        return AdminAccount.builder()
                .username(username)
                .password(password)
                .active(true)
                .createdByAdminId(createdByAdminId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void changePassword(String encodedPassword, OffsetDateTime now) {
        this.password = encodedPassword;
        this.updatedAt = now;
    }

    public void deactivate(OffsetDateTime now) {
        this.active = false;
        this.updatedAt = now;
    }

    public void activate(OffsetDateTime now) {
        this.active = true;
        this.updatedAt = now;
    }
}
