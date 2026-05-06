package com.tmk.api.adapter.out.ai;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.openai.client.OpenAIClient;
import com.openai.services.blocking.EmbeddingService;
import com.tmk.core.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenAiEmbeddingAdapterTest {

    @Mock
    private OpenAIClient openAIClient;

    @Mock
    private EmbeddingService embeddingService;

    private OpenAiEmbeddingAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OpenAiEmbeddingAdapter(openAIClient);
    }

    @Test
    @DisplayName("embed throws BusinessException when API call fails")
    void embed_throwsBusinessException_whenApiFails() {
        when(openAIClient.embeddings()).thenReturn(embeddingService);
        when(embeddingService.create(any())).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> adapter.embed(List.of("test text")))
                .isInstanceOf(BusinessException.class);
    }
}
