package io;

import model.RawEmbedding;

import java.io.IOException;
import java.util.List;

public interface EmbeddingLoader {

    /**
     * Loads embeddings from the source associated with this loader.
     */
    List<RawEmbedding> load() throws IOException;
}



