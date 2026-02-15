import java.util.List;

public interface EmbeddingSpaceFactory {

    /**
     * Builds an EmbeddingSpace from raw embeddings.
     */
    EmbeddingSpace createSpace(List<RawEmbedding> rawEmbeddings);
}

