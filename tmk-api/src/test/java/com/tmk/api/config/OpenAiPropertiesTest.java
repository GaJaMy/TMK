package com.tmk.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenAiPropertiesTest {

    @Test
    @DisplayName("OpenAiProperties has correct defaults")
    void properties_hasCorrectDefaults() {
        OpenAiProperties props = new OpenAiProperties();

        assertThat(props.getEmbedding().getModel()).isEqualTo("text-embedding-3-small");
        assertThat(props.getChat().getModel()).isEqualTo("gpt-4o-mini");
        assertThat(props.getChat().getTemperature()).isEqualTo(0.7);
        assertThat(props.getChat().getMaxTokens()).isEqualTo(4096);
    }
}
