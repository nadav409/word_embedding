import java.util.*;

public class NearestNeighborsOperation extends ResearchOperation {

    private final String key;
    private final int k;

    public NearestNeighborsOperation(Provider provider, SpaceId spaceId, String key, int k) {
        super(provider, spaceId);

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key cannot be null or blank");
        }

        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }

        this.key = key;
        this.k = k;
    }

    @Override
    protected OperationResult run(EmbeddingSpace space) {

        Embedding query = space.get(key);
        if (query == null) {
            throw new UnknownWordException(key);
        }

        List<Neighbor> topK = space.findNearest(query.getVector(), k, Set.of(key), metric());

        return new NearestNeighborsResult(key, k, topK);
    }
}