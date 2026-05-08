package com.tmk.api.user.document.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tmk.core.port.out.ai.DocumentReaderPort;
import com.tmk.core.port.out.ai.EmbeddingPort;
import com.tmk.core.port.out.ai.PrivateQuestionGenerationPort;
import com.tmk.core.port.out.persistence.DocumentChunkPort;
import com.tmk.core.port.out.persistence.DocumentPort;
import com.tmk.core.port.out.persistence.PrivateQuestionPort;
import com.tmk.core.port.out.persistence.UserAccountPort;
import com.tmk.core.port.out.storage.FileStoragePort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.vo.PrivateQuestionDraft;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(MockitoExtension.class)
class AsyncDocumentQuestionGenerationServiceTest {

    @Mock
    private DocumentPort documentPort;

    @Mock
    private DocumentChunkPort documentChunkPort;

    @Mock
    private PrivateQuestionPort privateQuestionPort;

    @Mock
    private UserAccountPort userAccountPort;

    @Mock
    private DocumentReaderPort documentReaderPort;

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private PrivateQuestionGenerationPort privateQuestionGenerationPort;

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private DocumentSseService documentSseService;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AsyncDocumentQuestionGenerationService asyncDocumentQuestionGenerationService;

    @Test
    void sanitizeDraftsNormalizesAndFiltersInvalidOptions() {
        List<PrivateQuestionDraft> drafts = List.of(
                new PrivateQuestionDraft(
                        " 객관식 문제 ",
                        QuestionType.MULTIPLE_CHOICE,
                        Difficulty.NORMAL,
                        " 정답 ",
                        " 해설 ",
                        List.of("1", "2", "3", "4", "5", "6")
                ),
                new PrivateQuestionDraft(
                        "OX 문제",
                        QuestionType.TRUE_FALSE,
                        Difficulty.EASY,
                        "O",
                        "해설",
                        List.of("참", "거짓", "기타")
                ),
                new PrivateQuestionDraft(
                        "단답형 문제",
                        QuestionType.SHORT_ANSWER,
                        Difficulty.HARD,
                        "정답",
                        "해설",
                        List.of("불필요한 선택지")
                ),
                new PrivateQuestionDraft(
                        "객관식 부족",
                        QuestionType.MULTIPLE_CHOICE,
                        Difficulty.NORMAL,
                        "정답",
                        "해설",
                        List.of("1", "2", "3", "4")
                )
        );

        List<PrivateQuestionDraft> sanitizedDrafts = asyncDocumentQuestionGenerationService.sanitizeDrafts(drafts);

        assertThat(sanitizedDrafts).hasSize(3);
        assertThat(sanitizedDrafts.get(0).options()).containsExactly("1", "2", "3", "4", "5");
        assertThat(sanitizedDrafts.get(1).options()).containsExactly("참", "거짓");
        assertThat(sanitizedDrafts.get(2).options()).isEmpty();
        assertThat(sanitizedDrafts.get(0).content()).isEqualTo("객관식 문제");
        assertThat(sanitizedDrafts.get(0).answer()).isEqualTo("정답");
        assertThat(sanitizedDrafts.get(0).explanation()).isEqualTo("해설");
    }
}
