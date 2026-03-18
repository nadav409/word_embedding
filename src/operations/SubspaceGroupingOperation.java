package operations;

import app.Provider;
import model.SpaceId;
import model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SubspaceGroupingOperation extends ResearchOperation {

    private final List<String> keys;
    private final int k;

    public SubspaceGroupingOperation(
            Provider provider,
            SpaceId spaceId,
            List<String> keys,
            int k
    ) {
        super(provider, spaceId);

        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("group cannot be empty");
        }

        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }

        this.keys = List.copyOf(keys);
        this.k = k;
    }

    @Override
    protected OperationResult run(EmbeddingSpace space) {

        // ===== 1️⃣ Collect vectors =====
        List<Vector> vectors = new ArrayList<>();

        for (String key : keys) {
            Embedding e = space.get(key);
            if (e == null) {
                throw new UnknownWordException(key);
            }
            vectors.add(e.getVector());
        }

        // ===== 2️⃣ Compute centroid =====
        Vector centroid = computeCentroid(vectors);

        // ===== 3️⃣ Compute distances =====
        List<Neighbor> neighbors = new ArrayList<>();

        for (Embedding e : space.getAll()) {

            // אם אתה לא רוצה שהקבוצה עצמה תופיע:
            if (keys.contains(e.getKey())) continue;

            double dist = metric().compute(centroid, e.getVector());

            neighbors.add(new Neighbor(e.getKey(), dist));
        }

        // ===== 4️⃣ Sort & take K =====
        neighbors.sort(Comparator.comparingDouble(Neighbor::getDistance));

        if (k < neighbors.size()) {
            neighbors = neighbors.subList(0, k);
        }

        return new SubspaceGroupingResult(neighbors);
    }

    private Vector computeCentroid(List<Vector> vectors) {

        int dim = vectors.get(0).dimension();
        double[] sum = new double[dim];

        for (Vector v : vectors) {
            for (int i = 0; i < dim; i++) {
                sum[i] += v.get(i);
            }
        }

        int n = vectors.size();

        for (int i = 0; i < dim; i++) {
            sum[i] /= n;
        }

        return new Vector(sum);
    }
}
