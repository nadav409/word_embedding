import java.util.Objects;

/**
 * Base class for all research operations.
 * Holds the Provider and chooses which EmbeddingSpace to run on.
 */
public abstract class ResearchOperation {

    protected final Provider provider;
    protected final SpaceId spaceId;

    protected ResearchOperation(Provider provider, SpaceId spaceId) {
        if (provider == null) {
            throw new IllegalArgumentException("provider cannot be null");
        }
        if (spaceId == null) {
            throw new IllegalArgumentException("spaceId cannot be null");
        }
        this.provider = provider;
        this.spaceId = spaceId;
    }

    /**
     * Template method: fetch the requested space and run the concrete operation.
     */
    public final OperationResult execute() {
        EmbeddingSpace space = provider.getSpace(spaceId);
        if (space == null) {
            // normally provider should never return null, but keep it safe
            throw new IllegalStateException("space is null for id: " + spaceId);
        }
        return run(space);
    }

    /**
     * Concrete operations implement their logic here.
     */
    protected abstract OperationResult run(EmbeddingSpace space);

    /**
     * The current metric/distance strategy to use (comes from Provider).
     */
    protected final DistanceStrategy metric() {
        DistanceStrategy s = provider.getDistanceStrategy();
        if (s == null) {
            throw new IllegalStateException("provider returned null DistanceStrategy");
        }
        return s;
    }
}
