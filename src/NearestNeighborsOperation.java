import java.util.List;
import java.util.Set;

public class NearestNeighborsOperation extends ResearchOperation {

    private final String key;
    private final int k;

    public NearestNeighborsOperation(Provider provider, SpaceId spaceId, String key, int k) {
        super(provider, spaceId);

        if (key == null || key.isBlank()) throw new IllegalArgumentException("key cannot be null/blank");
        if (k <= 0) throw new IllegalArgumentException("k must be positive");

        this.key = key;
        this.k = k;
    }

    public NearestNeighborsOperation(Provider provider, String key, int k) {
        this(provider, SpaceId.FULL, key, k);
    }

    @Override
    protected OperationResult run(EmbeddingSpace space) {
        Embedding query = space.get(key);
        if (query == null) throw new UnknownWordException(key);

        List<Neighbor> topK = NeighborFinder.findNearestByVector(
                space,
                query.getVector(),
                k,
                Set.of(key),
                metric()
        );

        return new NearestNeighborsResult(key, k, topK);
    }
}
