import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        DistanceStrategy strategy = metric();

        List<Neighbor> list = new ArrayList<>();
        for (Embedding candidate : space.getAll()) {
            String candidateKey = candidate.getKey();

            if (key.equals(candidateKey)) {
                continue; // לא להחזיר את עצמו
            }

            double d = strategy.compute(query.getVector(), candidate.getVector());
            list.add(new Neighbor(candidateKey, d));
        }

        list.sort(Comparator.comparingDouble(Neighbor::getDistance));

        int end = Math.min(k, list.size());
        List<Neighbor> topK = new ArrayList<>(list.subList(0, end));

        return new NearestNeighborsResult(key, k, topK);
    }
}

