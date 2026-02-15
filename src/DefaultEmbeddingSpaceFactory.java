import java.util.ArrayList;
import java.util.List;

public class DefaultEmbeddingSpaceFactory implements EmbeddingSpaceFactory {

    private final EmbeddingFactory embeddingFactory;

    public DefaultEmbeddingSpaceFactory(EmbeddingFactory embeddingFactory) {
        if (embeddingFactory == null) {
            throw new IllegalArgumentException("embeddingFactory cannot be null");
        }
        this.embeddingFactory = embeddingFactory;
    }

    @Override
    public EmbeddingSpace createSpace(List<RawEmbedding> rawEmbeddings) {
        if (rawEmbeddings == null || rawEmbeddings.isEmpty()) {
            throw new IllegalArgumentException("rawEmbeddings cannot be null/empty");
        }

        List<AbstractEmbedding> built = new ArrayList<>(rawEmbeddings.size());
        for (RawEmbedding raw : rawEmbeddings) {
            built.add(embeddingFactory.create(raw));
        }

        return new EmbeddingSpace(built);
    }
}
