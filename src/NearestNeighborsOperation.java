import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class NearestNeighborsOperation extends ResearchOperation {

    private final String key;
    private final int k;

    public NearestNeighborsOperation(Provider provider, SpaceId spaceId, String key, int k) {
        super(provider, spaceId);

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key cannot be null/blank");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }

        this.key = key;
        this.k = k;
    }

    public NearestNeighborsOperation(Provider provider, String key, int k) {
        this(provider, SpaceId.FULL, key, k);
    }

    @Override
    protected OperationResult run(EmbeddingSpace space) {
        Embedding query = space.get(key);
        if (query == null) {
            throw new UnknownWordException(key);
        }

        List<Neighbor> topK = findNearestByVector(
                space,
                query.getVector(),
                k,
                Set.of(key),
                metric()
        );

        return new NearestNeighborsResult(key, k, topK);
    }

    // ✅ reuse: חיפוש K שכנים לוקטור כללי
    public static List<Neighbor> findNearestByVector(
            EmbeddingSpace space,
            Vector queryVector,
            int k,
            Set<String> excludedKeys,
            DistanceStrategy strategy
    ) {
        if (space == null) throw new IllegalArgumentException("space is null");
        if (queryVector == null) throw new IllegalArgumentException("queryVector is null");
        if (strategy == null) throw new IllegalArgumentException("strategy is null");
        if (k <= 0) throw new IllegalArgumentException("k must be positive");

        List<Neighbor> list = new ArrayList<>();
        for (Embedding candidate : space.getAll()) {
            String candidateKey = candidate.getKey();

            if (excludedKeys != null && excludedKeys.contains(candidateKey)) {
                continue;
            }

            double d = strategy.compute(queryVector, candidate.getVector());
            list.add(new Neighbor(candidateKey, d));
        }

        list.sort(Comparator.comparingDouble(Neighbor::getDistance));

        int end = Math.min(k, list.size());
        return new ArrayList<>(list.subList(0, end));
    }
}
