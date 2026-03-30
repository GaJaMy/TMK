package com.tmk.core.document.service;

import com.tmk.core.document.entity.Document;
import com.tmk.core.document.vo.DocumentStatusInfo;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.DocumentChunkPort;
import com.tmk.core.port.out.DocumentPort;
import com.tmk.core.port.out.QuestionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetDocumentStatusService {

    private final DocumentPort documentPort;
    private final DocumentChunkPort documentChunkPort;
    private final QuestionPort questionPort;

    public DocumentStatusInfo getStatus(Long documentId) {
        Document document = documentPort.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));
        long chunkCount = documentChunkPort.countByDocumentId(documentId);
        long questionCount = questionPort.countByDocumentId(documentId);
        return new DocumentStatusInfo(document, chunkCount, questionCount);
    }
}
