import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class EmbeddingSpace {

    private final Map<String, Embedding> items;
    private final int dimension;

    public EmbeddingSpace(Collection<? extends Embedding> embeddings) {

        if (embeddings == null) {
            throw new IllegalArgumentException("embeddings cannot be null");
        }
        if (embeddings.isEmpty()) {
            throw new IllegalArgumentException("embeddings cannot be empty");
        }

        this.items = new HashMap<>();

        int dim = -1;

        for (Embedding e : embeddings) {

            if (e == null) {
                throw new IllegalArgumentException("embedding cannot be null");
            }

            // קביעת המימד לפי הראשון
            if (dim == -1) {
                dim = e.dimension();
                if (dim <= 0) {
                    throw new IllegalArgumentException("invalid embedding dimension: " + dim);
                }
            }
            else if (e.dimension() != dim) {
                throw new IllegalArgumentException(
                        "dimension mismatch for key '" + e.getKey() +
                                "': expected " + dim + " but got " + e.dimension()
                );
            }

            String key = e.getKey();

            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("embedding key cannot be null or blank");
            }

            if (items.containsKey(key)) {
                throw new IllegalArgumentException("duplicate embedding key: " + key);
            }

            items.put(key, e);
        }

        this.dimension = dim;
    }

    public int dimension() {
        return dimension;
    }

    public int size() {
        return items.size();
    }

    public boolean contains(String key) {
        return items.containsKey(key);
    }

    public Embedding get(String key) {
        return items.get(key);
    }

    public Collection<Embedding> getAll() {
        return items.values();
    }

    public List<Neighbor> findNearest(Vector queryVector, int k, Set<String> excludedKeys, DistanceStrategy strategy) {

        if (queryVector == null)
            throw new IllegalArgumentException("queryVector is null");

        if (strategy == null)
            throw new IllegalArgumentException("strategy is null");

        if (k <= 0)
            throw new IllegalArgumentException("k must be positive");

        List<Neighbor> neighbors = new ArrayList<>();

        // מעבר על כל המילים במרחב
        for (Embedding candidate : items.values()) {

            String candidateKey = candidate.getKey();

            // דילוג על מילים שלא רוצים להחזיר (למשל המילה עצמה)
            if (excludedKeys != null && excludedKeys.contains(candidateKey)) {
                continue;
            }

            // חישוב המרחק בין הוקטורים
            double distance = strategy.compute(queryVector, candidate.getVector());

            neighbors.add(new Neighbor(candidateKey, distance));
        }

        // מיון לפי מרחק (הכי קטן = הכי קרוב)
        Collections.sort(neighbors, new Comparator<Neighbor>() {
            @Override
            public int compare(Neighbor a, Neighbor b) {
                return Double.compare(a.getDistance(), b.getDistance());
            }
        });

        // חיתוך ל-k
        int end = Math.min(k, neighbors.size());
        List<Neighbor> topK = new ArrayList<>();

        for (int i = 0; i < end; i++) {
            topK.add(neighbors.get(i));
        }

        return topK;
    }
}
