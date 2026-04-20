package com.tmk.api.adapter.out.ai;

import com.openai.client.OpenAIClient;
import com.tmk.api.config.OpenAiProperties;
import com.tmk.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiQuestionGenerationAdapterTest {

    @Mock
    private OpenAIClient openAIClient;

    private OpenAiProperties openAiProperties;
    private OpenAiQuestionGenerationAdapter adapter;

    @BeforeEach
    void setUp() {
        openAiProperties = new OpenAiProperties();
        adapter = new OpenAiQuestionGenerationAdapter(openAIClient, openAiProperties);
    }

    @Test
    @DisplayName("generateQuestions throws BusinessException when API call fails")
    void generateQuestions_throwsBusinessException_whenApiFails() {
        when(openAIClient.chat()).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> adapter.generateQuestions(1L, "test context"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("OpenAiProperties has correct defaults")
    void properties_hasCorrectDefaults() {
        OpenAiProperties props = new OpenAiProperties();
        org.assertj.core.api.Assertions.assertThat(props.getEmbedding().getModel())
                .isEqualTo("text-embedding-3-small");
        org.assertj.core.api.Assertions.assertThat(props.getChat().getModel())
                .isEqualTo("gpt-4o-mini");
        org.assertj.core.api.Assertions.assertThat(props.getChat().getTemperature())
                .isEqualTo(0.7);
        org.assertj.core.api.Assertions.assertThat(props.getChat().getMaxTokens())
                .isEqualTo(4096);
    }
}
