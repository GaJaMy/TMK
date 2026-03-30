package com.tmk.core.port.out;

public interface EmbeddingPort {
    float[] embed(String text);
    int getDimension();
}
