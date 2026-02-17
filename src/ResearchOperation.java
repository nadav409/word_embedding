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

    public final OperationResult execute() {
        EmbeddingSpace space = provider.getSpace(spaceId);
        if (space == null) {
            throw new IllegalStateException("Embedding space not found: " + spaceId);
        }
        return run(space);
    }

    protected DistanceStrategy metric() {
        DistanceStrategy strategy = provider.getDistanceStrategy();
        if (strategy == null) {
            throw new IllegalStateException("Distance strategy is not set");
        }
        return strategy;
    }

    protected abstract OperationResult run(EmbeddingSpace space);
}
