package com.tmk.core.document.service;

import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentChunk;
import com.tmk.core.document.entity.DocumentStatus;
import com.tmk.core.document.vo.GeneratedQuestion;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.DocumentChunkPort;
import com.tmk.core.port.out.DocumentPort;
import com.tmk.core.port.out.EmbeddingPort;
import com.tmk.core.port.out.QuestionGenerationPort;
import com.tmk.core.port.out.QuestionPort;
import com.tmk.core.port.out.TextExtractionPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionOption;
import com.tmk.core.question.entity.QuestionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentProcessingServiceTest {

    @Mock
    private DocumentPort documentPort;

    @Mock
    private DocumentChunkPort documentChunkPort;

    @Mock
    private QuestionPort questionPort;

    @Mock
    private TextExtractionPort textExtractionPort;

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private QuestionGenerationPort questionGenerationPort;

    @InjectMocks
    private DocumentProcessingService documentProcessingService;

    private Document buildDocument(DocumentStatus status) {
        return Document.builder()
                .title("test")
                .source("/test.pdf")
                .status(status)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void process_validDocument_completesSuccessfully() {
        // Arrange
        Long documentId = 1L;
        Document document = buildDocument(DocumentStatus.PENDING);
        GeneratedQuestion generatedQuestion = new GeneratedQuestion(
                "What is Java?",
                QuestionType.SHORT_ANSWER,
                Difficulty.EASY,
                "A programming language",
                "Java is an object-oriented programming language.",
                null
        );

        when(documentPort.findById(documentId)).thenReturn(Optional.of(document));
        when(documentPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(textExtractionPort.extract("/test.pdf")).thenReturn("test text content for processing");
        when(embeddingPort.embed(any())).thenReturn(new float[1536]);
        when(questionGenerationPort.generateQuestions(any(), any())).thenReturn(List.of(generatedQuestion));
        when(questionPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        documentProcessingService.process(documentId);

        // Assert
        verify(documentPort, org.mockito.Mockito.atLeast(2)).save(any());
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.COMPLETED);

        verify(documentChunkPort).saveAll(any());
        verify(questionPort).save(any());
    }

    @Test
    void process_documentNotFound_throwsException() {
        // Arrange
        Long documentId = 999L;
        when(documentPort.findById(documentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> documentProcessingService.process(documentId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    @Test
    void process_externalFailure_setsStatusToFailed() {
        // Arrange
        Long documentId = 1L;
        Document document = buildDocument(DocumentStatus.PENDING);

        when(documentPort.findById(documentId)).thenReturn(Optional.of(document));
        when(documentPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(textExtractionPort.extract(any())).thenThrow(new RuntimeException("extraction failed"));

        // Act & Assert
        assertThatThrownBy(() -> documentProcessingService.process(documentId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DOCUMENT_PROCESSING_FAILED));

        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentPort, org.mockito.Mockito.atLeast(1)).save(documentCaptor.capture());

        List<Document> savedDocuments = documentCaptor.getAllValues();
        assertThat(savedDocuments).anyMatch(d -> d.getStatus() == DocumentStatus.FAILED);
    }
}
