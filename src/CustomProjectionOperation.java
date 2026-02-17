import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CustomProjectionOperation extends ResearchOperation {

    private final String a;
    private final String b;
    private final int k;

    // default: run on FULL space (recommended for semantic axes)
    public CustomProjectionOperation(Provider provider, String a, String b, int k) {
        this(provider, SpaceId.FULL, a, b, k);
    }

    public CustomProjectionOperation(Provider provider, SpaceId spaceId, String a, String b, int k) {
        super(provider, spaceId);

        if (a == null || a.isBlank()) throw new IllegalArgumentException("a cannot be null/blank");
        if (b == null || b.isBlank()) throw new IllegalArgumentException("b cannot be null/blank");
        if (a.equals(b)) throw new IllegalArgumentException("a and b must be different");
        if (k <= 0) throw new IllegalArgumentException("k must be positive");

        this.a = a;
        this.b = b;
        this.k = k;
    }

    @Override
    protected OperationResult run(EmbeddingSpace space) {

        Embedding ea = space.get(a);
        if (ea == null) throw new UnknownWordException(a);

        Embedding eb = space.get(b);
        if (eb == null) throw new UnknownWordException(b);

        Vector va = ea.getVector();
        Vector vb = eb.getVector();

        // axis = vb - va  (A -> B)
        Vector axis = vb.sub(va);
        double axisNorm = axis.norm();
        if (axisNorm == 0) {
            throw new IllegalArgumentException("Axis is zero (vectors identical?)");
        }

        List<CustomProjectionItem> scored = new ArrayList<>(space.size());

        for (Embedding e : space.getAll()) {
            String key = e.getKey();
            if (key == null) continue;

            // usually we exclude endpoints
            if (key.equals(a) || key.equals(b)) continue;

            Vector v = e.getVector();

            // scalar projection score onto axis direction:
            // score = dot(v, axis) / ||axis||
            double score = v.dot(axis) / axisNorm;

            scored.add(new CustomProjectionItem(key, score));
        }

        // sort ascending: lowest = most A-like, highest = most B-like
        scored.sort(Comparator.comparingDouble(CustomProjectionItem::getScore));

        int n = scored.size();
        int kk = Math.min(k, n);

        List<CustomProjectionItem> topA = new ArrayList<>(kk);
        for (int i = 0; i < kk; i++) {
            topA.add(scored.get(i));
        }

        List<CustomProjectionItem> topB = new ArrayList<>(kk);
        for (int i = 0; i < kk; i++) {
            topB.add(scored.get(n - 1 - i));
        }

        return new CustomProjectionResult(a, b, kk, topA, topB);
    }
}

