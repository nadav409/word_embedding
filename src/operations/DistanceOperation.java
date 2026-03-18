package operations;

import app.Provider;
import model.SpaceId;
import model.Embedding;
import model.EmbeddingSpace;
import model.UnknownWordException;

public class DistanceOperation extends ResearchOperation {

    private final String keyA;
    private final String keyB;

    public DistanceOperation(Provider provider, SpaceId spaceId, String keyA, String keyB) {
        super(provider, spaceId);

        if (keyA == null || keyA.isBlank()) {
            throw new IllegalArgumentException("keyA cannot be null/blank");
        }
        if (keyB == null || keyB.isBlank()) {
            throw new IllegalArgumentException("keyB cannot be null/blank");
        }

        this.keyA = keyA;
        this.keyB = keyB;
    }

    public DistanceOperation(Provider provider, String keyA, String keyB) {
        this(provider, SpaceId.FULL, keyA, keyB);
    }

    @Override
    protected OperationResult run(EmbeddingSpace space) {

        Embedding a = space.get(keyA);
        if (a == null) {
            throw new UnknownWordException(keyA);
        }

        Embedding b = space.get(keyB);
        if (b == null) {
            throw new UnknownWordException(keyB);
        }

        double distance = metric().compute(a.getVector(), b.getVector());
        return new DistanceResult(keyA, keyB, distance);
    }
}
