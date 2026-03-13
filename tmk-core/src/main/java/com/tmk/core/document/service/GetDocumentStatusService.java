package com.tmk.core.document.service;

import com.tmk.core.document.entity.Document;
import com.tmk.core.port.out.DocumentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetDocumentStatusService {

    private final DocumentPort documentPort;

    public Document getStatus(Long documentId) {
        // TODO
        return null;
    }
}
