package com.tmk.api.adapter.out.ai;

import com.tmk.core.port.out.ai.EmbeddingPort;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
public class StubEmbeddingAdapter implements EmbeddingPort {

    private static final int DIMENSION = 1536;

    @Override
    public List<float[]> embed(List<String> texts) {
        return texts.stream()
                .map(text -> new float[DIMENSION])
                .toList();
    }
}
