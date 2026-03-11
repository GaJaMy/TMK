package com.tmk.core.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "\"user\"")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}