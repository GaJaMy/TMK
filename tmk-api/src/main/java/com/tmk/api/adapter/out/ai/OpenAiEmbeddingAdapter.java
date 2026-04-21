package com.tmk.api.adapter.out.ai;

import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ai.EmbeddingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("!local & !test")
@RequiredArgsConstructor
public class OpenAiEmbeddingAdapter implements EmbeddingPort {

    private static final int DIMENSION = 1536;

    private final OpenAIClient openAIClient;

    @Override
    public float[] embed(String text) {
        try {
            EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                    .input(text)
                    .model(com.openai.models.embeddings.EmbeddingModel.TEXT_EMBEDDING_3_SMALL)
                    .build();

            CreateEmbeddingResponse response = openAIClient.embeddings().create(params);

            List<Double> vector = response.data().getFirst().embedding();
            float[] result = new float[vector.size()];
            for (int i = 0; i < vector.size(); i++) {
                result[i] = vector.get(i).floatValue();
            }

            log.debug("Generated embedding with {} dimensions", result.length);
            return result;
        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }

    @Override
    public int getDimension() {
        return DIMENSION;
    }
}
