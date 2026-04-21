package com.tmk.core.port.out.persistence;

import com.tmk.core.document.entity.Document;

import java.util.Optional;

public interface DocumentPort {

    Optional<Document> findById(Long id);

    Optional<Document> findPrivateByIdAndOwnerUserId(Long id, Long ownerUserId);

    Document save(Document document);
}
