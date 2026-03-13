package com.tmk.core.document.service;

import com.tmk.core.document.entity.Document;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.DocumentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetDocumentStatusService {

    private final DocumentPort documentPort;

    public Document getStatus(Long documentId) {
        return documentPort.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));
    }
}
