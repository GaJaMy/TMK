package com.tmk.core.port.out.ai;

import java.util.List;

public interface EmbeddingPort {

    List<float[]> embed(List<String> texts);
}
