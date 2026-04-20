package com.tmk.api.adapter.out.ai;

import com.tmk.core.port.out.EmbeddingPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
public class StubEmbeddingAdapter implements EmbeddingPort {

    private static final int DIMENSION = 1536;

    @Override
    public float[] embed(String text) {
        return new float[DIMENSION];
    }

    @Override
    public int getDimension() {
        return DIMENSION;
    }
}
