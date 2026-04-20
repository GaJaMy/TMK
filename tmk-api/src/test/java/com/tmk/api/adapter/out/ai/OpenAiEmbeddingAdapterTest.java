package com.tmk.api.adapter.out.ai;

import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.Embedding;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.services.blocking.EmbeddingService;
import com.tmk.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    @DisplayName("getDimension returns 1536")
    void getDimension_returns1536() {
        assertThat(adapter.getDimension()).isEqualTo(1536);
    }

    @Test
    @DisplayName("embed throws BusinessException when API call fails")
    void embed_throwsBusinessException_whenApiFails() {
        when(openAIClient.embeddings()).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> adapter.embed("test text"))
                .isInstanceOf(BusinessException.class);
    }
}
