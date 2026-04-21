package com.tmk.core.document.service;

import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentStatus;
import com.tmk.core.port.out.persistence.DocumentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RegisterDocumentService {

    private final DocumentPort documentPort;

    @Transactional
    public Document register(String title, String source, Topic topic, ContentScope scope, Long ownerUserId) {
        Document document = Document.builder()
                .title(title)
                .source(source)
                .ownerUserId(ownerUserId)
                .scope(scope)
                .topic(topic)
                .status(DocumentStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();
        return documentPort.save(document);
    }
}
