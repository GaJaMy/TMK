package com.tmk.core.port.out.ai;

public interface EmbeddingPort {
    float[] embed(String text);
    int getDimension();
}
