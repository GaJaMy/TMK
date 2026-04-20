package com.tmk.core.document.service;

import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentStatus;
import com.tmk.core.document.vo.DocumentStatusInfo;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.DocumentChunkPort;
import com.tmk.core.port.out.DocumentPort;
import com.tmk.core.port.out.QuestionPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDocumentStatusServiceTest {

    @Mock
    private DocumentPort documentPort;

    @Mock
    private DocumentChunkPort documentChunkPort;

    @Mock
    private QuestionPort questionPort;

    @InjectMocks
    private GetDocumentStatusService getDocumentStatusService;

    @Test
    void getStatus_existingDocument_returnsStatusInfo() {
        // Arrange
        Long documentId = 1L;
        Document document = Document.builder()
                .title("test")
                .source("/test.pdf")
                .status(DocumentStatus.COMPLETED)
                .createdAt(OffsetDateTime.now())
                .build();
        when(documentPort.findById(documentId)).thenReturn(Optional.of(document));
        when(documentChunkPort.countByDocumentId(documentId)).thenReturn(10L);
        when(questionPort.countByDocumentId(documentId)).thenReturn(5L);

        // Act
        DocumentStatusInfo result = getDocumentStatusService.getStatus(documentId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.document()).isEqualTo(document);
        assertThat(result.chunkCount()).isEqualTo(10L);
        assertThat(result.questionCount()).isEqualTo(5L);
    }

    @Test
    void getStatus_documentNotFound_throwsException() {
        // Arrange
        Long documentId = 999L;
        when(documentPort.findById(documentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getDocumentStatusService.getStatus(documentId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DOCUMENT_NOT_FOUND));
    }
}
