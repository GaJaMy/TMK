package com.tmk.core.port.out;

import com.tmk.core.document.entity.Document;

import java.util.Optional;

public interface DocumentPort {

    Optional<Document> findById(Long id);

    Document save(Document document);
}
