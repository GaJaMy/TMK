package com.tmk.core.document.service;

import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentStatus;
import com.tmk.core.port.out.DocumentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RegisterDocumentService {

    private final DocumentPort documentPort;

    public Document register(String title, String source) {
        Document document = Document.builder()
                .title(title)
                .source(source)
                .status(DocumentStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();
        return documentPort.save(document);
    }
}
