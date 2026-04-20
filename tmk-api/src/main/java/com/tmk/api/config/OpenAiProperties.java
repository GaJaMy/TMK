package com.tmk.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    private String apiKey;

    private Embedding embedding = new Embedding();
    private Chat chat = new Chat();

    @Getter
    @Setter
    public static class Embedding {
        private String model = "text-embedding-3-small";
    }

    @Getter
    @Setter
    public static class Chat {
        private String model = "gpt-4o-mini";
        private double temperature = 0.7;
        private int maxTokens = 4096;
    }
}
