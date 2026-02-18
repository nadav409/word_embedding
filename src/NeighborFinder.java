import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class NeighborFinder {

    private NeighborFinder() { } // no instances

    public static List<Neighbor> findNearestByVector(EmbeddingSpace space, Vector queryVector, int k, Set<String> excludedKeys, DistanceStrategy strategy) {
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
