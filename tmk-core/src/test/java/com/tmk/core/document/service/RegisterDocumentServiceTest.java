package com.tmk.core.document.service;

import com.tmk.core.common.Topic;
import com.tmk.core.common.ContentScope;
import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentStatus;
import com.tmk.core.port.out.persistence.DocumentPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterDocumentServiceTest {

    @Mock
    private DocumentPort documentPort;

    @InjectMocks
    private RegisterDocumentService registerDocumentService;

    @Test
    void register_validInput_savesDocumentWithPendingStatus() {
        // Arrange
        String title = "Test Document";
        String source = "/test.pdf";
        when(documentPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);

        // Act
        registerDocumentService.register(title, source, Topic.SPRING, ContentScope.PRIVATE, 1L);

        // Assert
        verify(documentPort).save(captor.capture());
        Document saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo(title);
        assertThat(saved.getSource()).isEqualTo(source);
        assertThat(saved.getTopic()).isEqualTo(Topic.SPRING);
        assertThat(saved.getScope()).isEqualTo(ContentScope.PRIVATE);
        assertThat(saved.getOwnerUserId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void register_validInput_returnsDocument() {
        // Arrange
        String title = "Test Document";
        String source = "/test.pdf";
        when(documentPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Document result = registerDocumentService.register(title, source, Topic.SPRING, ContentScope.PRIVATE, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getSource()).isEqualTo(source);
        assertThat(result.getTopic()).isEqualTo(Topic.SPRING);
        assertThat(result.getScope()).isEqualTo(ContentScope.PRIVATE);
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }
}
