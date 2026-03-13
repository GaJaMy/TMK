package com.tmk.core.document.repository;

import com.tmk.core.document.entity.Document;
import com.tmk.core.port.out.DocumentPort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long>, DocumentPort {
}
