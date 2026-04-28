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

    private final OpenAIClient openAIClient;

    @Override
    public List<float[]> embed(List<String> texts) {
        try {
            EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                    .inputOfArrayOfStrings(texts)
                    .model(com.openai.models.embeddings.EmbeddingModel.TEXT_EMBEDDING_3_SMALL)
                    .build();

            CreateEmbeddingResponse response = openAIClient.embeddings().create(params);
            List<float[]> embeddings = response.data().stream()
                    .map(item -> {
                        List<Double> vector = item.embedding();
                        float[] result = new float[vector.size()];
                        for (int i = 0; i < vector.size(); i++) {
                            result[i] = vector.get(i).floatValue();
                        }
                        return result;
                    })
                    .toList();

            log.debug("Generated {} embeddings", embeddings.size());
            return embeddings;
        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }
}
