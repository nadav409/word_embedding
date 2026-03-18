package operations;

import app.Provider;
import app.SpaceId;
import model.Embedding;
import model.EmbeddingSpace;
import model.UnknownWordException;
import model.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CustomProjectionOperation extends ResearchOperation {

    private final String a;
    private final String b;

    public CustomProjectionOperation(Provider provider, String a, String b) {
        this(provider, SpaceId.FULL, a, b);
    }

    public CustomProjectionOperation(Provider provider, SpaceId spaceId, String a, String b) {
        super(provider, spaceId);

        if (a == null || a.isBlank()) {
            throw new IllegalArgumentException("a cannot be null or blank");
        }

        if (b == null || b.isBlank()) {
            throw new IllegalArgumentException("b cannot be null or blank");
        }

        if (a.equals(b)) {
            throw new IllegalArgumentException("a and b must be different");
        }

        this.a = a;
        this.b = b;
    }

    @Override
    protected OperationResult run(EmbeddingSpace space) {

        Embedding embeddingA = space.get(a);
        if (embeddingA == null) {
            throw new UnknownWordException(a);
        }

        Embedding embeddingB = space.get(b);
        if (embeddingB == null) {
            throw new UnknownWordException(b);
        }

        Vector vectorA = embeddingA.getVector();
        Vector vectorB = embeddingB.getVector();

        Vector axis = vectorB.sub(vectorA);
        double axisNorm = axis.norm();

        if (axisNorm == 0) {
            throw new IllegalArgumentException("Axis is zero");
        }

        List<CustomProjectionItem> items = new ArrayList<>();

        for (Embedding embedding : space.getAll()) {
            String key = embedding.getKey();

            if (key == null || key.isBlank()) {
                continue;
            }

            Vector vector = embedding.getVector();

            // projection of (v - va) on the axis from A to B
            double score = vector.sub(vectorA).dot(axis) / axisNorm;

            items.add(new CustomProjectionItem(key, score));
        }

        items.sort(Comparator.comparingDouble(CustomProjectionItem::getScore));

        return new CustomProjectionResult(a, b, items);
    }
}