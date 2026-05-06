package com.tmk.api.user.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.tmk.api.user.document.result.DocumentStatusResult;
import com.tmk.api.user.document.result.DocumentUploadResult;
import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentStatus;
import com.tmk.core.document.entity.DocumentSourceType;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.DocumentPort;
import com.tmk.core.port.out.storage.FileStoragePort;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentPort documentPort;

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void uploadDocumentSavesPdfDocument() {
        OffsetDateTime now = OffsetDateTime.now();
        byte[] fileBytes = "pdf".getBytes();
        Document savedDocument = Document.builder()
                .id(11L)
                .userId(7L)
                .title("Spring Notes")
                .sourceType(DocumentSourceType.PDF_UPLOAD)
                .sourceReference("/tmp/documents/spring-notes.pdf")
                .status(DocumentStatus.PROCESSING)
                .generatedQuestionCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(fileStoragePort.store("spring-notes.pdf", fileBytes))
                .willReturn("/tmp/documents/spring-notes.pdf");
        given(documentPort.save(any(Document.class))).willReturn(savedDocument);

        DocumentUploadResult result = documentService.uploadDocument(7L, "Spring Notes", "spring-notes.pdf", fileBytes);

        assertThat(result).isEqualTo(new DocumentUploadResult(11L, DocumentStatus.PROCESSING));
        then(fileStoragePort).should().store("spring-notes.pdf", fileBytes);
        then(documentPort).should().save(any(Document.class));
    }

    @Test
    void uploadDocumentSavesMarkdownDocument() {
        OffsetDateTime now = OffsetDateTime.now();
        byte[] fileBytes = "# title".getBytes();
        Document savedDocument = Document.builder()
                .id(12L)
                .userId(7L)
                .title("Markdown Notes")
                .sourceType(DocumentSourceType.MD_UPLOAD)
                .sourceReference("/tmp/documents/notes.md")
                .status(DocumentStatus.PROCESSING)
                .generatedQuestionCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(fileStoragePort.store("notes.md", fileBytes))
                .willReturn("/tmp/documents/notes.md");
        given(documentPort.save(any(Document.class))).willReturn(savedDocument);

        DocumentUploadResult result = documentService.uploadDocument(7L, "Markdown Notes", "notes.md", fileBytes);

        assertThat(result).isEqualTo(new DocumentUploadResult(12L, DocumentStatus.PROCESSING));
    }

    @Test
    void uploadDocumentThrowsWhenDocumentSourceIsUnsupported() {
        byte[] fileBytes = "content".getBytes();

        assertThatThrownBy(() -> documentService.uploadDocument(7L, "Notes", "notes.txt", fileBytes))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.UNSUPPORTED_DOCUMENT_SOURCE.getMessage());
    }

    @Test
    void uploadDocumentThrowsWhenFileBytesAreEmpty() {
        assertThatThrownBy(() -> documentService.uploadDocument(7L, "Notes", "notes.md", new byte[0]))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_INPUT.getMessage());
    }

    @Test
    void getDocumentsReturnsCurrentUserDocuments() {
        OffsetDateTime now = OffsetDateTime.now();
        List<Document> documents = List.of(
                Document.builder()
                        .id(13L)
                        .userId(7L)
                        .title("Spring Notes")
                        .sourceType(DocumentSourceType.PDF_UPLOAD)
                        .sourceReference("/tmp/documents/spring-notes.pdf")
                        .status(DocumentStatus.PROCESSING)
                        .generatedQuestionCount(0)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                Document.builder()
                        .id(12L)
                        .userId(7L)
                        .title("JPA Notes")
                        .sourceType(DocumentSourceType.MD_UPLOAD)
                        .sourceReference("/tmp/documents/jpa-notes.md")
                        .status(DocumentStatus.COMPLETED)
                        .generatedQuestionCount(8)
                        .createdAt(now.minusDays(1))
                        .updatedAt(now)
                        .build()
        );

        given(documentPort.findAllByUserIdOrderByCreatedAtDesc(7L)).willReturn(documents);

        List<DocumentStatusResult> results = documentService.getDocuments(7L);

        assertThat(results).hasSize(2);
        assertThat(results.getFirst().documentId()).isEqualTo(13L);
        assertThat(results.get(1).generatedQuestionCount()).isEqualTo(8);
    }

    @Test
    void getDocumentStatusReturnsOwnedDocumentStatus() {
        OffsetDateTime now = OffsetDateTime.now();
        Document document = Document.builder()
                .id(11L)
                .userId(7L)
                .title("Spring Notes")
                .sourceType(DocumentSourceType.PDF_UPLOAD)
                .sourceReference("/tmp/documents/spring-notes.pdf")
                .status(DocumentStatus.COMPLETED)
                .generatedQuestionCount(12)
                .createdAt(now.minusMinutes(10))
                .updatedAt(now)
                .build();

        given(documentPort.findByIdAndUserId(11L, 7L)).willReturn(Optional.of(document));

        DocumentStatusResult result = documentService.getDocumentStatus(7L, 11L);

        assertThat(result.documentId()).isEqualTo(11L);
        assertThat(result.status()).isEqualTo(DocumentStatus.COMPLETED);
        assertThat(result.generatedQuestionCount()).isEqualTo(12);
    }

    @Test
    void getDocumentStatusThrowsWhenDocumentIsMissing() {
        given(documentPort.findByIdAndUserId(11L, 7L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocumentStatus(7L, 11L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DOCUMENT_NOT_FOUND.getMessage());

        then(documentPort).should().findByIdAndUserId(eq(11L), eq(7L));
    }
}
