package com.tmk.core.user.entity;

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
@Table(name = "\"user\"")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "country_code", nullable = false, length = 10)
    private String countryCode;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static UserAccount create(String username, String password, String countryCode, OffsetDateTime now) {
        return UserAccount.builder()
                .username(username)
                .password(password)
                .active(true)
                .countryCode(countryCode)
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

    public void touch(OffsetDateTime now) {
        this.updatedAt = now;
    }
}
