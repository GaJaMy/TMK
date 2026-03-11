package com.tmk.core.emailverification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "email_verification")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "expired_at", nullable = false)
    private OffsetDateTime expiredAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public void verify() {
        this.verified = true;
    }
}
