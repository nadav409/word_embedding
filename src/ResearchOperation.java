/**
 * Base class for all research operations.
 * Each operation asks the Provider for an EmbeddingSpace
 * and performs a calculation on it.
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
     * Main entry point of every operation.
     * 1) Ask the Provider for the requested space
     * 2) Run the specific operation logic
     */
    public final OperationResult execute() {

        // Step 1: get the embedding space from the provider
        EmbeddingSpace space = provider.getSpace(spaceId);

        if (space == null) {
            // Provider is not expected to return null, but we guard anyway
            throw new IllegalStateException("No space found for id: " + spaceId);
        }

        // Step 2: perform the actual operation (implemented by subclasses)
        OperationResult result = run(space);

        return result;
    }

    /**
     * Each concrete operation implements its calculation here.
     */
    protected abstract OperationResult run(EmbeddingSpace space);

    /**
     * Returns the distance strategy currently selected in the Provider.
     * (Cosine distance / Euclidean distance)
     */
    protected final DistanceStrategy metric() {

        DistanceStrategy strategy = provider.getDistanceStrategy();

        if (strategy == null) {
            throw new IllegalStateException("Provider returned null DistanceStrategy");
        }

        return strategy;
    }
}