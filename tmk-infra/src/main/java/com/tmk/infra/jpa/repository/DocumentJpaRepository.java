package com.tmk.infra.jpa.repository;

import com.tmk.core.common.ContentScope;
import com.tmk.core.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentJpaRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByIdAndScopeAndOwnerUserId(Long id, ContentScope scope, Long ownerUserId);
}
