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
        private String model;
    }

    @Getter
    @Setter
    public static class Chat {
        private String model;
        private double temperature;
        private int maxTokens;
    }
}
